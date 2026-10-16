package com.example.foodapp.data;

import android.content.Context;
import android.widget.Toast;

import androidx.lifecycle.LifecycleOwner;

import com.example.foodapp.api.ApiCallback;
import com.example.foodapp.api.ApiClient;
import com.example.foodapp.model.Cart;
import com.example.foodapp.model.FavoriteResponse;
import com.example.foodapp.model.Food;
import com.example.foodapp.model.User;

import java.util.HashMap;
import java.util.Map;

/** Các thao tác dùng ở nhiều màn hình: thêm vào giỏ, bật/tắt yêu thích, làm mới giỏ hàng. */
public final class FoodActions {

    public interface Done<T> {
        void onDone(T result);
    }

    private FoodActions() {}

    /** done nhận Cart khi thành công, null khi lỗi (lỗi đã được báo bằng Toast). */
    public static void addToCart(Context context, LifecycleOwner owner, Food food, int quantity, Done<Cart> done) {
        Map<String, Object> body = new HashMap<>();
        body.put("foodId", food.getId());
        body.put("quantity", quantity);
        ApiClient.get().addToCart(body).enqueue(new ApiCallback<Cart>(owner) {
            @Override
            public void onSuccess(Cart cart) {
                AppState.updateCart(cart);
                if (done != null) done.onDone(cart);
            }

            @Override
            public void onError(String message) {
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
                if (done != null) done.onDone(null);
            }
        });
    }

    /** Cập nhật giao diện ngay (optimistic), nếu API lỗi thì hoàn tác. */
    public static void toggleFavorite(Context context, LifecycleOwner owner, Food food, Done<Boolean> done) {
        String id = food.getId();
        boolean newState = !AppState.isFavorite(id);
        AppState.setFavorite(id, newState);
        if (done != null) done.onDone(newState);

        ApiClient.get().toggleFavorite(id).enqueue(new ApiCallback<FavoriteResponse>(null) {
            @Override
            public void onSuccess(FavoriteResponse data) {
                AppState.setFavorites(data.getFavorites());
                User user = SessionManager.get().getUser();
                if (user != null) {
                    user.setFavorites(data.getFavorites());
                    SessionManager.get().saveUser(user);
                }
            }

            @Override
            public void onError(String message) {
                AppState.setFavorite(id, !newState);
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
                if (done != null && isOwnerAlive(owner)) done.onDone(!newState);
            }
        });
    }

    public static void refreshCartCount() {
        if (!SessionManager.get().isLoggedIn()) return;
        ApiClient.get().getCart().enqueue(new ApiCallback<Cart>(null) {
            @Override
            public void onSuccess(Cart cart) {
                AppState.updateCart(cart);
            }

            @Override
            public void onError(String message) {
                // Không cần báo lỗi cho badge
            }
        });
    }

    private static boolean isOwnerAlive(LifecycleOwner owner) {
        return owner == null || owner.getLifecycle().getCurrentState()
                .isAtLeast(androidx.lifecycle.Lifecycle.State.CREATED);
    }
}
