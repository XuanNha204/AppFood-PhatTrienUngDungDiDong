package com.example.foodapp.api;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleOwner;

import com.example.foodapp.FoodApp;
import com.example.foodapp.R;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Callback dùng chung cho Retrofit:
 *  - Tự đọc thông báo lỗi {"message": "..."} từ backend
 *  - Bỏ qua kết quả nếu màn hình đã bị huỷ (tránh crash khi người dùng thoát giữa chừng)
 */
public abstract class ApiCallback<T> implements Callback<T> {

    private final LifecycleOwner owner;

    protected ApiCallback(LifecycleOwner owner) {
        this.owner = owner;
    }

    public abstract void onSuccess(T data);

    public abstract void onError(String message);

    /** Luôn được gọi sau onSuccess/onError — dùng để tắt loading. */
    public void onComplete() {}

    private boolean isAlive() {
        if (owner == null) return true;
        if (owner instanceof Fragment) {
            Fragment f = (Fragment) owner;
            if (!f.isAdded() || f.getView() == null) return false;
        }
        return owner.getLifecycle().getCurrentState() != Lifecycle.State.DESTROYED;
    }

    @Override
    public void onResponse(@NonNull Call<T> call, @NonNull Response<T> response) {
        if (call.isCanceled() || !isAlive()) return;
        if (response.isSuccessful() && response.body() != null) {
            onSuccess(response.body());
        } else {
            onError(parseError(response));
        }
        onComplete();
    }

    @Override
    public void onFailure(@NonNull Call<T> call, @NonNull Throwable t) {
        if (call.isCanceled() || !isAlive()) return;
        // IOException = lỗi mạng; còn lại thường là JSON trả về không khớp model
        onError(t instanceof java.io.IOException
                ? FoodApp.get().getString(R.string.error_network)
                : "Dữ liệu từ máy chủ không hợp lệ");
        onComplete();
    }

    private String parseError(Response<T> response) {
        try (ResponseBody body = response.errorBody()) {
            if (body != null) {
                JsonObject json = JsonParser.parseString(body.string()).getAsJsonObject();
                if (json.has("message")) return json.get("message").getAsString();
            }
        } catch (Exception ignored) {
            // Không phải JSON -> dùng thông báo mặc định
        }
        if (response.code() == 401) return FoodApp.get().getString(R.string.session_expired);
        return "Có lỗi xảy ra (mã " + response.code() + ")";
    }
}
