package com.example.foodapp.ui.favorite;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;

import com.example.foodapp.R;
import com.example.foodapp.api.ApiCallback;
import com.example.foodapp.api.ApiClient;
import com.example.foodapp.databinding.FragmentFavoritesBinding;
import com.example.foodapp.model.Food;
import com.example.foodapp.ui.common.EmptyState;
import com.example.foodapp.ui.common.FoodAdapter;
import com.example.foodapp.ui.common.FoodListController;
import com.example.foodapp.ui.main.MainActivity;
import com.example.foodapp.ui.main.TabFragment;

import java.util.List;

public class FavoritesFragment extends Fragment implements TabFragment {
    private FragmentFavoritesBinding binding;
    private FoodAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentFavoritesBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        FoodListController controller = FoodListController.of(this);
        adapter = new FoodAdapter(controller);
        controller.attach(adapter);
        // Bỏ yêu thích -> xoá khỏi danh sách ngay
        controller.setOnFavoriteChanged((food, position, isFavorite) -> {
            if (!isFavorite) {
                adapter.removeAt(position);
                if (adapter.getItemCount() == 0) showEmpty();
            }
        });

        binding.recyclerView.setLayoutManager(new GridLayoutManager(requireContext(), 2));
        binding.recyclerView.setAdapter(adapter);
        binding.swipeRefresh.setColorSchemeResources(R.color.primary);
        binding.swipeRefresh.setOnRefreshListener(this::load);
        binding.swipeRefresh.setRefreshing(true);
        load();
    }

    private void load() {
        ApiClient.get().getFavorites().enqueue(new ApiCallback<List<Food>>(this) {
            @Override
            public void onSuccess(List<Food> data) {
                adapter.setItems(data);
                binding.recyclerView.scheduleLayoutAnimation();
                if (data.isEmpty()) showEmpty();
                else EmptyState.hide(binding.empty);
            }

            @Override
            public void onError(String message) {
                EmptyState.show(binding.empty, R.drawable.ic_favorite, "Không tải được dữ liệu", message,
                        getString(R.string.retry), v -> load());
            }

            @Override
            public void onComplete() {
                binding.swipeRefresh.setRefreshing(false);
            }
        });
    }

    private void showEmpty() {
        EmptyState.show(binding.empty, R.drawable.ic_favorite, getString(R.string.empty_favorites_title),
                getString(R.string.empty_favorites_message), getString(R.string.explore_foods),
                v -> ((MainActivity) requireActivity()).selectTab(R.id.nav_home));
    }

    @Override
    public void onTabSelected() {
        load();
    }

    @Override
    public void onTabReselected() {
        binding.recyclerView.smoothScrollToPosition(0);
    }

    @Override
    public void onDestroyView() {
        binding = null;
        super.onDestroyView();
    }
}
