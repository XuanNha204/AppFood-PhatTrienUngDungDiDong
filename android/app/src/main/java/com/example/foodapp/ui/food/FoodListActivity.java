package com.example.foodapp.ui.food;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.EditorInfo;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodapp.R;
import com.example.foodapp.api.ApiCallback;
import com.example.foodapp.api.ApiClient;
import com.example.foodapp.data.AppState;
import com.example.foodapp.databinding.ActivityFoodListBinding;
import com.example.foodapp.model.Food;
import com.example.foodapp.ui.common.BaseActivity;
import com.example.foodapp.ui.common.EmptyState;
import com.example.foodapp.ui.common.FoodAdapter;
import com.example.foodapp.ui.common.FoodListController;
import com.example.foodapp.util.UiUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;

/**
 * Danh sách món ăn, dùng cho nhiều chế độ:
 * theo danh mục, tìm kiếm, tất cả, phổ biến, và "món tôi đã đăng".
 */
public class FoodListActivity extends BaseActivity {
    private static final String EXTRA_MODE = "mode";
    private static final String EXTRA_CATEGORY_ID = "category_id";
    private static final String EXTRA_TITLE = "title";

    private static final String MODE_ALL = "all";
    private static final String MODE_SEARCH = "search";
    private static final String MODE_POPULAR = "popular";
    private static final String MODE_CATEGORY = "category";
    private static final String MODE_MINE = "mine";

    private ActivityFoodListBinding binding;
    private FoodAdapter adapter;
    private FoodListController controller;
    private final Handler searchHandler = new Handler(Looper.getMainLooper());
    private Call<List<Food>> currentCall;

    private String mode;
    private String categoryId;
    private String sort = "";
    private int loadedFoodsVersion;

    // ----- Các cách mở màn hình -----
    public static void startCategory(Context context, String categoryId, String categoryName) {
        context.startActivity(new Intent(context, FoodListActivity.class)
                .putExtra(EXTRA_MODE, MODE_CATEGORY)
                .putExtra(EXTRA_CATEGORY_ID, categoryId)
                .putExtra(EXTRA_TITLE, categoryName));
    }

    public static void startSearch(Context context) {
        context.startActivity(new Intent(context, FoodListActivity.class)
                .putExtra(EXTRA_MODE, MODE_SEARCH)
                .putExtra(EXTRA_TITLE, "Tìm kiếm"));
    }

    public static void startAll(Context context) {
        context.startActivity(new Intent(context, FoodListActivity.class)
                .putExtra(EXTRA_MODE, MODE_ALL)
                .putExtra(EXTRA_TITLE, context.getString(R.string.all_foods)));
    }

    public static void startPopular(Context context) {
        context.startActivity(new Intent(context, FoodListActivity.class)
                .putExtra(EXTRA_MODE, MODE_POPULAR)
                .putExtra(EXTRA_TITLE, context.getString(R.string.section_popular)));
    }

    public static void startMine(Context context) {
        context.startActivity(new Intent(context, FoodListActivity.class)
                .putExtra(EXTRA_MODE, MODE_MINE)
                .putExtra(EXTRA_TITLE, context.getString(R.string.my_foods)));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityFoodListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mode = getIntent().getStringExtra(EXTRA_MODE);
        if (mode == null) mode = MODE_ALL;
        categoryId = getIntent().getStringExtra(EXTRA_CATEGORY_ID);
        setupToolbar(binding.toolbar, getIntent().getStringExtra(EXTRA_TITLE));

        controller = new FoodListController(this, this, binding.getRoot());
        adapter = new FoodAdapter(controller);
        controller.attach(adapter);
        binding.recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        binding.recyclerView.setAdapter(adapter);

        // Thu gọn nút "Thêm món" khi cuộn xuống
        binding.recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView rv, int dx, int dy) {
                if (dy > 6) binding.fabAdd.shrink();
                else if (dy < -6) binding.fabAdd.extend();
            }
        });

        binding.fabAdd.setOnClickListener(v -> AddEditFoodActivity.startCreate(this, categoryId));
        binding.swipeRefresh.setColorSchemeResources(R.color.primary);
        binding.swipeRefresh.setOnRefreshListener(this::load);

        if (MODE_POPULAR.equals(mode)) {
            binding.chipPopular.setChecked(true);
            sort = "popular";
        }
        if (MODE_MINE.equals(mode)) {
            binding.filterBar.setVisibility(View.GONE);
        } else {
            setupSearchAndSort();
        }

        load();
    }

    private void setupSearchAndSort() {
        // Tìm kiếm tự động sau khi ngừng gõ 400ms
        binding.etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                searchHandler.removeCallbacksAndMessages(null);
                searchHandler.postDelayed(FoodListActivity.this::load, 400);
            }
        });
        binding.etSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                searchHandler.removeCallbacksAndMessages(null);
                UiUtils.hideKeyboard(this);
                load();
                return true;
            }
            return false;
        });

        binding.chipSort.setOnCheckedStateChangeListener((group, checkedIds) -> {
            int id = checkedIds.isEmpty() ? R.id.chipDefault : checkedIds.get(0);
            if (id == R.id.chipPopular) sort = "popular";
            else if (id == R.id.chipRating) sort = "rating";
            else if (id == R.id.chipPriceAsc) sort = "price_asc";
            else if (id == R.id.chipPriceDesc) sort = "price_desc";
            else if (id == R.id.chipNewest) sort = "newest";
            else sort = "";
            load();
        });

        if (MODE_SEARCH.equals(mode)) {
            binding.etSearch.requestFocus();
            binding.etSearch.postDelayed(() -> {
                android.view.inputmethod.InputMethodManager imm = (android.view.inputmethod.InputMethodManager)
                        getSystemService(INPUT_METHOD_SERVICE);
                imm.showSoftInput(binding.etSearch, 0);
            }, 300);
        }
    }

    private void load() {
        loadedFoodsVersion = AppState.foodsVersion();
        if (currentCall != null) currentCall.cancel();
        EmptyState.hide(binding.empty);
        if (adapter.getItemCount() == 0 && !binding.swipeRefresh.isRefreshing()) {
            binding.shimmerGrid.shimmer.setVisibility(View.VISIBLE);
            binding.shimmerGrid.shimmer.startShimmer();
        }

        if (MODE_MINE.equals(mode)) {
            currentCall = ApiClient.get().getMyFoods();
        } else {
            Map<String, String> query = new HashMap<>();
            String keyword = binding.etSearch.getText() == null ? "" : binding.etSearch.getText().toString().trim();
            if (!keyword.isEmpty()) query.put("search", keyword);
            if (categoryId != null) query.put("category", categoryId);
            if (!sort.isEmpty()) query.put("sort", sort);
            currentCall = ApiClient.get().getFoods(query);
        }

        currentCall.enqueue(new ApiCallback<List<Food>>(this) {
            @Override
            public void onSuccess(List<Food> data) {
                adapter.setItems(data);
                binding.recyclerView.scheduleLayoutAnimation();
                binding.tvResultCount.setText(data.size() + " món");
                if (data.isEmpty()) showEmpty();
            }

            @Override
            public void onError(String message) {
                adapter.setItems(null);
                binding.tvResultCount.setText("");
                EmptyState.show(binding.empty, R.drawable.ic_restaurant, "Không tải được dữ liệu", message,
                        getString(R.string.retry), v -> load());
            }

            @Override
            public void onComplete() {
                binding.swipeRefresh.setRefreshing(false);
                binding.shimmerGrid.shimmer.stopShimmer();
                binding.shimmerGrid.shimmer.setVisibility(View.GONE);
            }
        });
    }

    private void showEmpty() {
        if (MODE_MINE.equals(mode)) {
            EmptyState.show(binding.empty, R.drawable.ic_restaurant, "Bạn chưa đăng món nào",
                    "Chia sẻ món ngon của bạn kèm hình ảnh nhé!", getString(R.string.action_add_food),
                    v -> AddEditFoodActivity.startCreate(this, null));
        } else {
            EmptyState.show(binding.empty, R.drawable.ic_search, getString(R.string.empty_foods_title),
                    getString(R.string.empty_foods_message), null, null);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        controller.refreshFavorites();
        if (loadedFoodsVersion != AppState.foodsVersion()) load();
    }

    @Override
    protected void onDestroy() {
        searchHandler.removeCallbacksAndMessages(null);
        if (currentCall != null) currentCall.cancel();
        super.onDestroy();
    }
}
