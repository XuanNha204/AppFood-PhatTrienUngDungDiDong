package com.example.foodapp.ui.category;

import android.os.Bundle;

import androidx.recyclerview.widget.GridLayoutManager;

import com.example.foodapp.R;
import com.example.foodapp.api.ApiCallback;
import com.example.foodapp.api.ApiClient;
import com.example.foodapp.databinding.ActivityCategoriesBinding;
import com.example.foodapp.model.Category;
import com.example.foodapp.ui.common.BaseActivity;
import com.example.foodapp.ui.common.CategoryAdapter;
import com.example.foodapp.ui.common.EmptyState;
import com.example.foodapp.ui.food.FoodListActivity;

import java.util.List;

/** Màn hình danh mục món ăn (dạng lưới, kèm số lượng món). */
public class CategoriesActivity extends BaseActivity {
    private ActivityCategoriesBinding binding;
    private CategoryAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCategoriesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setupToolbar(binding.toolbar, getString(R.string.all_categories));

        adapter = new CategoryAdapter(true, category ->
                FoodListActivity.startCategory(this, category.getId(), category.getName()));
        binding.recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        binding.recyclerView.setAdapter(adapter);

        binding.swipeRefresh.setColorSchemeResources(R.color.primary);
        binding.swipeRefresh.setOnRefreshListener(this::load);
        binding.swipeRefresh.setRefreshing(true);
        load();
    }

    private void load() {
        EmptyState.hide(binding.empty);
        ApiClient.get().getCategories().enqueue(new ApiCallback<List<Category>>(this) {
            @Override
            public void onSuccess(List<Category> data) {
                adapter.setItems(data);
                binding.recyclerView.scheduleLayoutAnimation();
                if (data.isEmpty()) {
                    EmptyState.show(binding.empty, R.drawable.ic_category, "Chưa có danh mục", "", null, null);
                }
            }

            @Override
            public void onError(String message) {
                EmptyState.show(binding.empty, R.drawable.ic_category, "Không tải được danh mục", message,
                        getString(R.string.retry), v -> {
                            binding.swipeRefresh.setRefreshing(true);
                            load();
                        });
            }

            @Override
            public void onComplete() {
                binding.swipeRefresh.setRefreshing(false);
            }
        });
    }
}
