package com.example.foodapp.ui.home;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.viewpager2.widget.ViewPager2;

import com.example.foodapp.R;
import com.example.foodapp.api.ApiCallback;
import com.example.foodapp.api.ApiClient;
import com.example.foodapp.data.AppState;
import com.example.foodapp.data.SessionManager;
import com.example.foodapp.databinding.FragmentHomeBinding;
import com.example.foodapp.model.Banner;
import com.example.foodapp.model.Category;
import com.example.foodapp.model.Food;
import com.example.foodapp.model.User;
import com.example.foodapp.ui.category.CategoriesActivity;
import com.example.foodapp.ui.common.CategoryAdapter;
import com.example.foodapp.ui.common.EmptyState;
import com.example.foodapp.ui.common.FoodAdapter;
import com.example.foodapp.ui.common.FoodListController;
import com.example.foodapp.ui.food.FoodListActivity;
import com.example.foodapp.ui.main.MainActivity;
import com.example.foodapp.ui.main.TabFragment;
import com.example.foodapp.util.ImageUtils;
import com.example.foodapp.util.UiUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HomeFragment extends Fragment implements TabFragment {
    private static final long BANNER_INTERVAL_MS = 3500;

    private FragmentHomeBinding binding;
    private final Handler bannerHandler = new Handler(Looper.getMainLooper());
    private BannerAdapter bannerAdapter;
    private CategoryAdapter categoryAdapter;
    private FoodAdapter popularAdapter;
    private FoodAdapter recommendedAdapter;
    private FoodListController popularController;
    private FoodListController recommendedController;
    private int loadedFoodsVersion = -1;
    private int pendingRequests;

    private final Runnable bannerAutoScroll = new Runnable() {
        @Override
        public void run() {
            if (binding == null || bannerAdapter.getItemCount() == 0) return;
            int next = (binding.bannerPager.getCurrentItem() + 1) % bannerAdapter.getItemCount();
            binding.bannerPager.setCurrentItem(next, true);
            bannerHandler.postDelayed(this, BANNER_INTERVAL_MS);
        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.swipeRefresh.setColorSchemeResources(R.color.primary);
        binding.swipeRefresh.setOnRefreshListener(this::loadData);
        // Nút "Trợ lý AI" thu gọn khi cuộn xuống, mở rộng khi cuộn lên
        binding.scrollView.setOnScrollChangeListener(
                (androidx.core.widget.NestedScrollView.OnScrollChangeListener) (v, x, y, oldX, oldY) -> {
                    if (Math.abs(y - oldY) > 4) ((MainActivity) requireActivity()).setChatFabExtended(y < oldY || y == 0);
                });

        binding.searchBox.setOnClickListener(v -> FoodListActivity.startSearch(requireContext()));
        binding.imgAvatar.setOnClickListener(v -> ((MainActivity) requireActivity()).selectTab(R.id.nav_profile));
        binding.tvAllCategories.setOnClickListener(v ->
                startActivity(new android.content.Intent(requireContext(), CategoriesActivity.class)));
        binding.tvAllPopular.setOnClickListener(v -> FoodListActivity.startPopular(requireContext()));
        binding.tvAllFoods.setOnClickListener(v -> FoodListActivity.startAll(requireContext()));

        setupBanner();

        categoryAdapter = new CategoryAdapter(false, category ->
                FoodListActivity.startCategory(requireContext(), category.getId(), category.getName()));
        binding.rvCategories.setLayoutManager(
                new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        binding.rvCategories.setAdapter(categoryAdapter);

        popularController = FoodListController.of(this);
        popularAdapter = new FoodAdapter(popularController, UiUtils.dp(requireContext(), 170));
        popularController.attach(popularAdapter);
        binding.rvPopular.setLayoutManager(
                new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        binding.rvPopular.setAdapter(popularAdapter);

        recommendedController = FoodListController.of(this);
        recommendedAdapter = new FoodAdapter(recommendedController);
        recommendedController.attach(recommendedAdapter);
        binding.rvRecommended.setLayoutManager(new GridLayoutManager(requireContext(), 2));
        binding.rvRecommended.setAdapter(recommendedAdapter);

        // Đổi trạng thái yêu thích ở 1 danh sách -> cập nhật danh sách còn lại
        popularController.setOnFavoriteChanged((food, pos, fav) -> recommendedController.refreshFavorites());
        recommendedController.setOnFavoriteChanged((food, pos, fav) -> popularController.refreshFavorites());

        bindUser();
        loadData();
    }

    private void setupBanner() {
        bannerAdapter = new BannerAdapter(this::openBanner);
        binding.bannerPager.setAdapter(bannerAdapter);
        binding.bannerPager.setOffscreenPageLimit(1);
        // Hiệu ứng thu nhỏ trang bên cạnh khi vuốt
        binding.bannerPager.setPageTransformer((page, position) -> {
            float scale = 1f - Math.abs(position) * 0.08f;
            page.setScaleY(scale);
            page.setAlpha(1f - Math.abs(position) * 0.3f);
        });
        binding.bannerPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                updateDots(position);
            }
        });
    }

    private void openBanner(Banner banner) {
        if (banner.getCategoryId() != null) {
            FoodListActivity.startCategory(requireContext(), banner.getCategoryId(), banner.getCategoryName());
        } else {
            FoodListActivity.startAll(requireContext());
        }
    }

    private void updateDots(int selected) {
        if (binding == null) return;
        LinearLayout dots = binding.bannerDots;
        dots.removeAllViews();
        int margin = UiUtils.dp(requireContext(), 3);
        for (int i = 0; i < bannerAdapter.getItemCount(); i++) {
            View dot = new View(requireContext());
            dot.setBackgroundResource(i == selected ? R.drawable.bg_dot_active : R.drawable.bg_dot_inactive);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    UiUtils.dp(requireContext(), i == selected ? 20 : 8), UiUtils.dp(requireContext(), 8));
            lp.setMargins(margin, 0, margin, 0);
            dots.addView(dot, lp);
        }
    }

    private void bindUser() {
        User user = SessionManager.get().getUser();
        if (user == null) return;
        String firstName = user.getName().trim();
        int space = firstName.lastIndexOf(' ');
        if (space >= 0) firstName = firstName.substring(space + 1);
        binding.tvGreeting.setText(getString(R.string.hello_user, firstName));
        ImageUtils.loadAvatar(binding.imgAvatar, user.getAvatarUrl());
    }

    private void loadData() {
        loadedFoodsVersion = AppState.foodsVersion();
        pendingRequests = 4;
        if (recommendedAdapter.getItemCount() == 0) {
            binding.shimmerGrid.shimmer.setVisibility(View.VISIBLE);
            binding.shimmerGrid.shimmer.startShimmer();
        }
        EmptyState.hide(binding.empty);

        ApiClient.get().getBanners().enqueue(new ApiCallback<List<Banner>>(this) {
            @Override
            public void onSuccess(List<Banner> data) {
                bannerAdapter.setItems(data);
                updateDots(binding.bannerPager.getCurrentItem());
                restartBannerTimer();
            }

            @Override
            public void onError(String message) {}

            @Override
            public void onComplete() { requestDone(); }
        });

        ApiClient.get().getCategories().enqueue(new ApiCallback<List<Category>>(this) {
            @Override
            public void onSuccess(List<Category> data) {
                categoryAdapter.setItems(data);
            }

            @Override
            public void onError(String message) {}

            @Override
            public void onComplete() { requestDone(); }
        });

        Map<String, String> popularQuery = new HashMap<>();
        popularQuery.put("popular", "true");
        popularQuery.put("sort", "popular");
        ApiClient.get().getFoods(popularQuery).enqueue(new ApiCallback<List<Food>>(this) {
            @Override
            public void onSuccess(List<Food> data) {
                popularAdapter.setItems(data);
            }

            @Override
            public void onError(String message) {}

            @Override
            public void onComplete() { requestDone(); }
        });

        Map<String, String> recommendedQuery = new HashMap<>();
        recommendedQuery.put("sort", "rating");
        recommendedQuery.put("limit", "10");
        ApiClient.get().getFoods(recommendedQuery).enqueue(new ApiCallback<List<Food>>(this) {
            @Override
            public void onSuccess(List<Food> data) {
                recommendedAdapter.setItems(data);
                binding.rvRecommended.scheduleLayoutAnimation();
            }

            @Override
            public void onError(String message) {
                if (recommendedAdapter.getItemCount() == 0) {
                    EmptyState.show(binding.empty, R.drawable.ic_restaurant, "Không tải được dữ liệu", message,
                            getString(R.string.retry), v -> loadData());
                } else {
                    UiUtils.toast(requireContext(), message);
                }
            }

            @Override
            public void onComplete() { requestDone(); }
        });
    }

    private void requestDone() {
        pendingRequests--;
        if (pendingRequests <= 0 && binding != null) {
            binding.swipeRefresh.setRefreshing(false);
            binding.shimmerGrid.shimmer.stopShimmer();
            binding.shimmerGrid.shimmer.setVisibility(View.GONE);
        }
    }

    private void restartBannerTimer() {
        bannerHandler.removeCallbacks(bannerAutoScroll);
        if (isResumed() && !isHidden()) bannerHandler.postDelayed(bannerAutoScroll, BANNER_INTERVAL_MS);
    }

    @Override
    public void onResume() {
        super.onResume();
        bindUser();
        popularController.refreshFavorites();
        recommendedController.refreshFavorites();
        if (loadedFoodsVersion != AppState.foodsVersion()) loadData();
        restartBannerTimer();
    }

    @Override
    public void onPause() {
        super.onPause();
        bannerHandler.removeCallbacks(bannerAutoScroll);
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (hidden) bannerHandler.removeCallbacks(bannerAutoScroll);
    }

    @Override
    public void onTabSelected() {
        bindUser();
        popularController.refreshFavorites();
        recommendedController.refreshFavorites();
        restartBannerTimer();
    }

    @Override
    public void onTabReselected() {
        binding.scrollView.smoothScrollTo(0, 0);
    }

    @Override
    public void onDestroyView() {
        bannerHandler.removeCallbacks(bannerAutoScroll);
        binding = null;
        super.onDestroyView();
    }
}
