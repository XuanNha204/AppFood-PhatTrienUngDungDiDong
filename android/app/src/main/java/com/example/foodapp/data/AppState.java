package com.example.foodapp.data;

import android.os.Handler;
import android.os.Looper;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.foodapp.model.Cart;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/** Trạng thái dùng chung giữa các màn hình: số món trong giỏ, danh sách yêu thích, ... */
public final class AppState {
    private static final MutableLiveData<Integer> cartCount = new MutableLiveData<>(0);
    private static final MutableLiveData<Boolean> sessionExpired = new MutableLiveData<>(false);
    private static final Set<String> favoriteIds = new HashSet<>();
    private static final Handler mainHandler = new Handler(Looper.getMainLooper());

    // Tăng mỗi khi món ăn được thêm/sửa/xoá để các danh sách biết cần tải lại
    private static int foodsVersion = 0;

    private AppState() {}

    // ----- Giỏ hàng -----
    public static LiveData<Integer> cartCount() {
        return cartCount;
    }

    public static void updateCart(Cart cart) {
        cartCount.setValue(cart == null ? 0 : cart.getTotalQuantity());
    }

    public static void resetCart() {
        cartCount.setValue(0);
    }

    // ----- Yêu thích -----
    public static synchronized void setFavorites(Collection<String> ids) {
        favoriteIds.clear();
        if (ids != null) favoriteIds.addAll(ids);
    }

    public static synchronized boolean isFavorite(String foodId) {
        return favoriteIds.contains(foodId);
    }

    public static synchronized void setFavorite(String foodId, boolean favorite) {
        if (favorite) favoriteIds.add(foodId);
        else favoriteIds.remove(foodId);
    }

    // ----- Món ăn thay đổi -----
    public static int foodsVersion() {
        return foodsVersion;
    }

    public static void markFoodsChanged() {
        foodsVersion++;
    }

    // ----- Phiên đăng nhập -----
    public static LiveData<Boolean> sessionExpired() {
        return sessionExpired;
    }

    public static void notifySessionExpired() {
        mainHandler.post(() -> sessionExpired.setValue(true));
    }

    public static void consumeSessionExpired() {
        sessionExpired.setValue(false);
    }

    public static void reset() {
        resetCart();
        setFavorites(null);
        consumeSessionExpired();
    }
}
