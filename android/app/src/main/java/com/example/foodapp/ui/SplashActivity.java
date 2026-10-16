package com.example.foodapp.ui;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.animation.AnimationUtils;
import android.view.animation.OvershootInterpolator;

import androidx.appcompat.app.AppCompatActivity;

import com.example.foodapp.R;
import com.example.foodapp.api.ApiCallback;
import com.example.foodapp.api.ApiClient;
import com.example.foodapp.data.SessionManager;
import com.example.foodapp.databinding.ActivitySplashBinding;
import com.example.foodapp.model.User;
import com.example.foodapp.ui.auth.LoginActivity;
import com.example.foodapp.ui.main.MainActivity;

@SuppressLint("CustomSplashScreen")
public class SplashActivity extends AppCompatActivity {
    private static final long MIN_DURATION_MS = 1400;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private long startTime;
    private boolean navigated;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivitySplashBinding binding = ActivitySplashBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        startTime = System.currentTimeMillis();

        // Logo phóng to + xoay nhẹ, chữ trượt lên
        binding.logo.setScaleX(0f);
        binding.logo.setScaleY(0f);
        binding.logo.setRotation(-90f);
        binding.logo.animate().scaleX(1f).scaleY(1f).rotation(0f).setDuration(700)
                .setInterpolator(new OvershootInterpolator(1.4f)).start();
        binding.tvAppName.startAnimation(AnimationUtils.loadAnimation(this, R.anim.fade_in_up));
        binding.tvTagline.startAnimation(AnimationUtils.loadAnimation(this, R.anim.fade_in_up));

        SessionManager session = SessionManager.get();
        if (!session.isLoggedIn()) {
            navigateAfterDelay(LoginActivity.class);
            return;
        }

        // Kiểm tra token còn hợp lệ và cập nhật thông tin user mới nhất
        ApiClient.get().getMe().enqueue(new ApiCallback<User>(this) {
            @Override
            public void onSuccess(User user) {
                session.saveUser(user);
                navigateAfterDelay(MainActivity.class);
            }

            @Override
            public void onError(String message) {
                // Token hết hạn -> interceptor đã xoá session; lỗi mạng -> vẫn vào app với dữ liệu cũ
                navigateAfterDelay(session.isLoggedIn() ? MainActivity.class : LoginActivity.class);
            }
        });
    }

    private void navigateAfterDelay(Class<?> target) {
        long remaining = Math.max(0, MIN_DURATION_MS - (System.currentTimeMillis() - startTime));
        handler.postDelayed(() -> {
            if (navigated || isFinishing()) return;
            navigated = true;
            startActivity(new Intent(this, target));
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            finish();
        }, remaining);
    }

    @Override
    protected void onDestroy() {
        handler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }
}
