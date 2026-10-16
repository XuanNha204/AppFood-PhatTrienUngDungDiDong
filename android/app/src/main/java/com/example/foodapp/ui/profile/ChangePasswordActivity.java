package com.example.foodapp.ui.profile;

import android.os.Bundle;

import com.example.foodapp.R;
import com.example.foodapp.api.ApiCallback;
import com.example.foodapp.api.ApiClient;
import com.example.foodapp.databinding.ActivityChangePasswordBinding;
import com.example.foodapp.model.MessageResponse;
import com.example.foodapp.ui.common.BaseActivity;
import com.example.foodapp.util.UiUtils;
import com.example.foodapp.util.Validator;

import java.util.HashMap;
import java.util.Map;

public class ChangePasswordActivity extends BaseActivity {
    private ActivityChangePasswordBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChangePasswordBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setupToolbar(binding.toolbar, getString(R.string.change_password));
        binding.btnSave.setOnClickListener(v -> save());
    }

    private void save() {
        boolean valid = Validator.required(binding.tilCurrent, "Vui lòng nhập mật khẩu hiện tại")
                && Validator.password(binding.tilNew)
                && Validator.matches(binding.tilConfirm, binding.tilNew);
        if (!valid) return;

        UiUtils.hideKeyboard(this);
        UiUtils.setLoading(binding.btnSave, true, getString(R.string.change_password));
        Map<String, String> body = new HashMap<>();
        body.put("currentPassword", Validator.text(binding.tilCurrent));
        body.put("newPassword", Validator.text(binding.tilNew));

        ApiClient.get().changePassword(body).enqueue(new ApiCallback<MessageResponse>(this) {
            @Override
            public void onSuccess(MessageResponse data) {
                UiUtils.toast(ChangePasswordActivity.this, data.getMessage());
                finish();
            }

            @Override
            public void onError(String message) {
                UiUtils.setLoading(binding.btnSave, false, getString(R.string.change_password));
                binding.tilCurrent.setError(message);
            }
        });
    }
}
