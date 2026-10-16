package com.example.foodapp.ui.common;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.foodapp.R;
import com.example.foodapp.data.AppState;
import com.example.foodapp.data.SessionManager;
import com.example.foodapp.ui.auth.LoginActivity;
import com.example.foodapp.util.UiUtils;

/** Activity cần đăng nhập: tự chuyển về màn Login khi token hết hạn / bị đăng xuất. */
public abstract class BaseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppState.sessionExpired().observe(this, expired -> {
            if (Boolean.TRUE.equals(expired)) {
                AppState.consumeSessionExpired();
                UiUtils.toast(this, getString(R.string.session_expired));
                goToLogin();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!SessionManager.get().isLoggedIn()) goToLogin();
    }

    protected void goToLogin() {
        AppState.reset();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    /** Gắn toolbar có nút quay lại. */
    protected void setupToolbar(Toolbar toolbar, String title) {
        toolbar.setTitle(title);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back);
        toolbar.setNavigationOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());
    }
}
