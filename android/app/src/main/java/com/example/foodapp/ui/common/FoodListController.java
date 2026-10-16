package com.example.foodapp.ui.common;

import android.app.Activity;
import android.view.View;
import android.widget.ImageView;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.LifecycleOwner;

import com.example.foodapp.R;
import com.example.foodapp.data.FoodActions;
import com.example.foodapp.model.Food;
import com.example.foodapp.ui.food.FoodDetailActivity;
import com.example.foodapp.ui.main.MainActivity;
import com.example.foodapp.util.UiUtils;
import com.google.android.material.snackbar.Snackbar;

/**
 * Xử lý các hành động chuẩn trên một item món ăn (mở chi tiết, thêm giỏ, yêu thích)
 * để không phải lặp lại code ở Home / FoodList / Favorites.
 */
public class FoodListController implements FoodAdapter.Listener {

    public interface FavoriteChanged {
        void onChanged(Food food, int position, boolean isFavorite);
    }

    private final Activity activity;
    private final LifecycleOwner owner;
    private final View snackbarAnchor;
    private FoodAdapter adapter;
    private FavoriteChanged favoriteChanged;

    public FoodListController(Activity activity, LifecycleOwner owner, View snackbarAnchor) {
        this.activity = activity;
        this.owner = owner;
        this.snackbarAnchor = snackbarAnchor;
    }

    public static FoodListController of(Fragment fragment) {
        return new FoodListController(fragment.requireActivity(), fragment, fragment.requireView());
    }

    public void attach(FoodAdapter adapter) {
        this.adapter = adapter;
    }

    public void setOnFavoriteChanged(FavoriteChanged callback) {
        this.favoriteChanged = callback;
    }

    @Override
    public void onFoodClick(Food food, ImageView sharedImage) {
        FoodDetailActivity.start(activity, food, sharedImage);
    }

    @Override
    public void onAddToCart(Food food, View button) {
        UiUtils.bounce(button);
        FoodActions.addToCart(activity, owner, food, 1, cart -> {
            if (cart == null) return;
            Snackbar snackbar = Snackbar.make(snackbarAnchor,
                    activity.getString(R.string.added_to_cart, 1, food.getName()), Snackbar.LENGTH_SHORT);
            if (!(activity instanceof MainActivity)) {
                snackbar.setAction(R.string.view_cart, v -> MainActivity.openTab(activity, R.id.nav_cart));
            }
            View nav = activity.findViewById(R.id.bottomNav);
            if (nav != null) snackbar.setAnchorView(nav);
            snackbar.show();
        });
    }

    @Override
    public void onToggleFavorite(Food food, int position) {
        FoodActions.toggleFavorite(activity, owner, food, isFavorite -> {
            if (adapter != null) adapter.notifyItemChanged(position);
            if (favoriteChanged != null) favoriteChanged.onChanged(food, position, isFavorite);
        });
    }

    /** Gọi trong onResume để cập nhật icon trái tim sau khi quay lại từ màn chi tiết. */
    public void refreshFavorites() {
        if (adapter != null && adapter.getItemCount() > 0) {
            adapter.notifyItemRangeChanged(0, adapter.getItemCount());
        }
    }
}
