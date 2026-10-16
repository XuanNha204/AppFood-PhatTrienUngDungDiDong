package com.example.foodapp.ui.auth;

import android.os.Bundle;
import android.view.animation.AnimationUtils;

import androidx.appcompat.app.AppCompatActivity;

import com.example.foodapp.R;
import com.example.foodapp.api.ApiCallback;
import com.example.foodapp.api.ApiClient;
import com.example.foodapp.databinding.ActivityRegisterBinding;
import com.example.foodapp.model.AuthResponse;
import com.example.foodapp.util.UiUtils;
import com.example.foodapp.util.Validator;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {
    private ActivityRegisterBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.toolbar.setNavigationIcon(R.drawable.ic_arrow_back);
        binding.toolbar.setNavigationOnClickListener(v -> finish());
        binding.form.startAnimation(AnimationUtils.loadAnimation(this, R.anim.fade_in_up));

        binding.btnRegister.setOnClickListener(v -> register());
        binding.tvLogin.setOnClickListener(v -> finish());
    }

    private void register() {
        boolean valid = Validator.required(binding.tilName, "Vui lòng nhập họ tên")
                && Validator.email(binding.tilEmail)
                && Validator.phone(binding.tilPhone, true)
                && Validator.password(binding.tilPassword)
                && Validator.matches(binding.tilConfirm, binding.tilPassword);
        if (!valid) return;

        UiUtils.hideKeyboard(this);
        UiUtils.setLoading(binding.btnRegister, true, getString(R.string.action_register));

        Map<String, String> body = new HashMap<>();
        body.put("name", Validator.text(binding.tilName));
        body.put("email", Validator.text(binding.tilEmail));
        body.put("phone", Validator.text(binding.tilPhone));
        body.put("password", Validator.text(binding.tilPassword));
        ApiClient.get().register(body).enqueue(new ApiCallback<AuthResponse>(this) {
            @Override
            public void onSuccess(AuthResponse data) {
                UiUtils.toast(RegisterActivity.this, "Đăng ký thành công! Chào mừng " + data.getUser().getName());
                AuthHelper.onAuthenticated(RegisterActivity.this, data);
            }

            @Override
            public void onError(String message) {
                UiUtils.setLoading(binding.btnRegister, false, getString(R.string.action_register));
                if (message.toLowerCase().contains("email")) binding.tilEmail.setError(message);
                else UiUtils.toast(RegisterActivity.this, message);
            }
        });
    }
}
