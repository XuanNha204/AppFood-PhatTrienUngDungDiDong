package com.example.foodapp.ui.food;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityOptionsCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.example.foodapp.R;
import com.example.foodapp.api.ApiCallback;
import com.example.foodapp.api.ApiClient;
import com.example.foodapp.data.AppState;
import com.example.foodapp.data.FoodActions;
import com.example.foodapp.data.SessionManager;
import com.example.foodapp.databinding.ActivityFoodDetailBinding;
import com.example.foodapp.model.Food;
import com.example.foodapp.model.MessageResponse;
import com.example.foodapp.ui.common.BaseActivity;
import com.example.foodapp.ui.main.MainActivity;
import com.example.foodapp.util.Format;
import com.example.foodapp.util.ImageUtils;
import com.example.foodapp.util.UiUtils;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;

public class FoodDetailActivity extends BaseActivity {
    private static final String EXTRA_FOOD_ID = "food_id";
    private static final String EXTRA_FOOD_JSON = "food_json";
    private static final int MAX_QUANTITY = 99;

    private ActivityFoodDetailBinding binding;
    private Food food;
    private String foodId;
    private int quantity = 1;
    private int loadedFoodsVersion;

    /** Mở chi tiết với hiệu ứng ảnh "bay" từ danh sách (shared element transition). */
    public static void start(Activity activity, Food food, @Nullable ImageView sharedImage) {
        Intent intent = new Intent(activity, FoodDetailActivity.class)
                .putExtra(EXTRA_FOOD_ID, food.getId())
                .putExtra(EXTRA_FOOD_JSON, new Gson().toJson(food));
        if (sharedImage != null) {
            ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                    activity, sharedImage, "food_image");
            activity.startActivity(intent, options.toBundle());
        } else {
            activity.startActivity(intent);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityFoodDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        supportPostponeEnterTransition();

        binding.toolbar.setNavigationIcon(R.drawable.ic_arrow_back);
        binding.toolbar.setNavigationOnClickListener(v -> supportFinishAfterTransition());
        binding.toolbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_edit) {
                AddEditFoodActivity.startEdit(this, food);
                return true;
            } else if (item.getItemId() == R.id.action_delete) {
                confirmDelete();
                return true;
            }
            return false;
        });

        foodId = getIntent().getStringExtra(EXTRA_FOOD_ID);
        String json = getIntent().getStringExtra(EXTRA_FOOD_JSON);
        if (json != null) {
            food = new Gson().fromJson(json, Food.class);
            bind(true);
        } else {
            supportStartPostponedEnterTransition();
        }

        binding.btnMinus.setOnClickListener(v -> changeQuantity(-1));
        binding.btnPlus.setOnClickListener(v -> changeQuantity(1));
        binding.btnAddToCart.setOnClickListener(v -> addToCart());
        binding.fabFavorite.setOnClickListener(v -> toggleFavorite());

        loadedFoodsVersion = AppState.foodsVersion();
        loadFood();
    }

    private void loadFood() {
        ApiClient.get().getFood(foodId).enqueue(new ApiCallback<Food>(this) {
            @Override
            public void onSuccess(Food data) {
                boolean first = food == null;
                food = data;
                bind(first);
            }

            @Override
            public void onError(String message) {
                UiUtils.toast(FoodDetailActivity.this, message);
                if (food == null) finish();
            }
        });
    }

    private void bind(boolean loadImage) {
        binding.collapsingToolbar.setTitle(food.getName());
        binding.tvName.setText(food.getName());
        binding.tvCategory.setText(food.getCategoryName());
        binding.tvCategory.setVisibility(food.getCategoryName().isEmpty() ? View.GONE : View.VISIBLE);
        binding.tvPopular.setVisibility(food.isPopular() ? View.VISIBLE : View.GONE);
        binding.tvPrice.setText(Format.money(food.getPrice()));
        binding.tvRating.setText(Format.rating(food.getRating()));
        binding.tvRatingCount.setText(getString(R.string.rating_count, food.getRatingCount()));
        binding.tvPrepTime.setText(getString(R.string.minutes, food.getPrepTime()));
        binding.tvCalories.setText(food.getCalories() > 0 ? getString(R.string.calories, food.getCalories()) : "--");
        binding.tvSold.setText(String.valueOf(food.getSoldCount()));
        binding.tvDescription.setText(food.getDescription().isEmpty()
                ? "Chưa có mô tả cho món ăn này." : food.getDescription());
        updateFavoriteIcon();
        updateQuantity();

        // Chỉ người đăng món mới thấy nút Sửa / Xoá
        binding.toolbar.getMenu().clear();
        String userId = SessionManager.get().getUserId();
        if (userId != null && userId.equals(food.getCreatedBy())) {
            binding.toolbar.inflateMenu(R.menu.menu_food_detail);
        }

        if (loadImage) loadImage();
    }

    private void loadImage() {
        Glide.with(this)
                .load(ImageUtils.fullUrl(food.getImageUrl()))
                .placeholder(R.drawable.bg_image_placeholder)
                .error(R.drawable.bg_image_placeholder)
                .dontAnimate()
                .centerCrop()
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model,
                                                @androidx.annotation.NonNull Target<Drawable> target,
                                                boolean isFirstResource) {
                        supportStartPostponedEnterTransition();
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(@androidx.annotation.NonNull Drawable resource,
                                                   @androidx.annotation.NonNull Object model,
                                                   Target<Drawable> target,
                                                   @androidx.annotation.NonNull DataSource dataSource,
                                                   boolean isFirstResource) {
                        supportStartPostponedEnterTransition();
                        return false;
                    }
                })
                .into(binding.imgFood);
    }

    private void changeQuantity(int delta) {
        int next = quantity + delta;
        if (next < 1 || next > MAX_QUANTITY) return;
        quantity = next;
        updateQuantity();
        UiUtils.bounce(binding.tvQuantity);
    }

    private void updateQuantity() {
        binding.tvQuantity.setText(String.valueOf(quantity));
        binding.btnMinus.setAlpha(quantity > 1 ? 1f : 0.35f);
        binding.btnAddToCart.setText(getString(R.string.add_to_cart_with_price,
                Format.money(food.getPrice() * quantity)));
    }

    private void updateFavoriteIcon() {
        binding.fabFavorite.setImageResource(AppState.isFavorite(food.getId())
                ? R.drawable.ic_favorite : R.drawable.ic_favorite_border);
    }

    private void toggleFavorite() {
        if (food == null) return;
        FoodActions.toggleFavorite(this, this, food, isFavorite -> {
            updateFavoriteIcon();
            UiUtils.bounce(binding.fabFavorite);
            if (isFavorite) Snackbar.make(binding.root, "Đã thêm vào yêu thích ❤", Snackbar.LENGTH_SHORT)
                    .setAnchorView(binding.bottomBar).show();
        });
    }

    private void addToCart() {
        if (food == null) return;
        binding.btnAddToCart.setEnabled(false);
        UiUtils.bounce(binding.btnAddToCart);
        int added = quantity;
        FoodActions.addToCart(this, this, food, added, cart -> {
            binding.btnAddToCart.setEnabled(true);
            if (cart == null) return;
            quantity = 1;
            updateQuantity();
            Snackbar.make(binding.root, getString(R.string.added_to_cart, added, food.getName()),
                            Snackbar.LENGTH_LONG)
                    .setAnchorView(binding.bottomBar)
                    .setAction(R.string.view_cart, v -> MainActivity.openTab(this, R.id.nav_cart))
                    .show();
        });
    }

    private void confirmDelete() {
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.delete)
                .setMessage(getString(R.string.confirm_delete_food, food.getName()))
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.delete, (d, w) -> deleteFood())
                .show();
    }

    private void deleteFood() {
        ApiClient.get().deleteFood(food.getId()).enqueue(new ApiCallback<MessageResponse>(this) {
            @Override
            public void onSuccess(MessageResponse data) {
                AppState.markFoodsChanged();
                FoodActions.refreshCartCount();
                UiUtils.toast(FoodDetailActivity.this, data.getMessage());
                finish();
            }

            @Override
            public void onError(String message) {
                UiUtils.toast(FoodDetailActivity.this, message);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Quay lại sau khi sửa món -> tải lại dữ liệu mới
        if (loadedFoodsVersion != AppState.foodsVersion()) {
            loadedFoodsVersion = AppState.foodsVersion();
            food = null;
            loadFood();
        }
    }
}
