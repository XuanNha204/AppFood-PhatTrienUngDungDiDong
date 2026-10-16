package com.example.foodapp.ui.auth;

import android.app.Activity;
import android.content.Intent;

import com.example.foodapp.data.AppState;
import com.example.foodapp.data.SessionManager;
import com.example.foodapp.model.AuthResponse;
import com.example.foodapp.ui.main.MainActivity;

final class AuthHelper {
    private AuthHelper() {}

    /** Lưu token + user rồi vào màn hình chính, xoá các màn auth khỏi back stack. */
    static void onAuthenticated(Activity activity, AuthResponse response) {
        AppState.reset();
        SessionManager.get().saveSession(response.getToken(), response.getUser());
        Intent intent = new Intent(activity, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        activity.startActivity(intent);
        activity.finish();
    }
}
