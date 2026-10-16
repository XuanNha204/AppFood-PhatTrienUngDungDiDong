package com.example.foodapp.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.view.inputmethod.EditorInfo;
import android.view.animation.AnimationUtils;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.example.foodapp.R;
import com.example.foodapp.api.ApiCallback;
import com.example.foodapp.api.ApiClient;
import com.example.foodapp.databinding.ActivityLoginBinding;
import com.example.foodapp.model.AuthResponse;
import com.example.foodapp.util.UiUtils;
import com.example.foodapp.util.Validator;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {
    public static final String EXTRA_EMAIL = "email";

    private ActivityLoginBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.primary));
        new WindowInsetsControllerCompat(getWindow(), getWindow().getDecorView())
                .setAppearanceLightStatusBars(false);

        binding.form.startAnimation(AnimationUtils.loadAnimation(this, R.anim.fade_in_up));
        UiUtils.bounce(binding.logo);

        String email = getIntent().getStringExtra(EXTRA_EMAIL);
        if (email != null && binding.tilEmail.getEditText() != null) {
            binding.tilEmail.getEditText().setText(email);
        }

        binding.btnLogin.setOnClickListener(v -> login());
        binding.etPassword.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                login();
                return true;
            }
            return false;
        });
        binding.tvRegister.setOnClickListener(v -> startActivity(new Intent(this, RegisterActivity.class)));
        binding.tvForgot.setOnClickListener(v -> {
            Intent intent = new Intent(this, ForgotPasswordActivity.class);
            intent.putExtra(EXTRA_EMAIL, Validator.text(binding.tilEmail));
            startActivity(intent);
        });
    }

    private void login() {
        if (!Validator.email(binding.tilEmail) || !Validator.password(binding.tilPassword)) return;
        UiUtils.hideKeyboard(this);
        UiUtils.setLoading(binding.btnLogin, true, getString(R.string.action_login));

        Map<String, String> body = new HashMap<>();
        body.put("email", Validator.text(binding.tilEmail));
        body.put("password", Validator.text(binding.tilPassword));
        ApiClient.get().login(body).enqueue(new ApiCallback<AuthResponse>(this) {
            @Override
            public void onSuccess(AuthResponse data) {
                UiUtils.toast(LoginActivity.this, "Xin chào " + data.getUser().getName() + "!");
                AuthHelper.onAuthenticated(LoginActivity.this, data);
            }

            @Override
            public void onError(String message) {
                UiUtils.setLoading(binding.btnLogin, false, getString(R.string.action_login));
                binding.tilPassword.setError(message);
            }
        });
    }
}
