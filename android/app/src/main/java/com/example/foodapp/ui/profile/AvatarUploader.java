package com.example.foodapp.ui.profile;

import android.content.Context;
import android.net.Uri;

import androidx.lifecycle.LifecycleOwner;

import com.example.foodapp.R;
import com.example.foodapp.api.ApiCallback;
import com.example.foodapp.api.ApiClient;
import com.example.foodapp.data.SessionManager;
import com.example.foodapp.model.User;
import com.example.foodapp.util.ImageUploadHelper;
import com.example.foodapp.util.UiUtils;

import java.util.Collections;

import okhttp3.MultipartBody;
import retrofit2.Call;

/** Upload avatar (nén ảnh trước khi gửi) hoặc đặt avatar bằng link, dùng ở màn Profile và Edit Profile. */
final class AvatarUploader {

    interface Callback {
        void onFinished(User updatedUserOrNull);
    }

    private AvatarUploader() {}

    static void upload(Context context, LifecycleOwner owner, Uri uri, Callback callback) {
        ImageUploadHelper.prepare(context, uri, "image", new ImageUploadHelper.Callback() {
            @Override
            public void onReady(MultipartBody.Part part) {
                send(context, owner, ApiClient.get().uploadAvatar(part), callback);
            }

            @Override
            public void onError(String message) {
                UiUtils.toast(context, message);
                callback.onFinished(null);
            }
        });
    }

    static void useLink(Context context, LifecycleOwner owner, String url, Callback callback) {
        send(context, owner, ApiClient.get().setAvatarLink(Collections.singletonMap("imageUrl", url)), callback);
    }

    private static void send(Context context, LifecycleOwner owner, Call<User> call, Callback callback) {
        call.enqueue(new ApiCallback<User>(owner) {
            @Override
            public void onSuccess(User user) {
                SessionManager.get().saveUser(user);
                UiUtils.toast(context, context.getString(R.string.avatar_updated));
                callback.onFinished(user);
            }

            @Override
            public void onError(String message) {
                UiUtils.toast(context, message);
                callback.onFinished(null);
            }
        });
    }
}
