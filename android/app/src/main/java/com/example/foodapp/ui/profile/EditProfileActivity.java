package com.example.foodapp.ui.profile;

import android.os.Bundle;
import android.view.View;

import com.google.android.material.textfield.TextInputLayout;
import com.example.foodapp.R;
import com.example.foodapp.api.ApiCallback;
import com.example.foodapp.api.ApiClient;
import com.example.foodapp.data.SessionManager;
import com.example.foodapp.databinding.ActivityEditProfileBinding;
import com.example.foodapp.model.User;
import com.example.foodapp.ui.common.BaseActivity;
import com.example.foodapp.ui.common.ImagePickerHelper;
import com.example.foodapp.util.ImageUtils;
import com.example.foodapp.util.UiUtils;
import com.example.foodapp.util.Validator;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class EditProfileActivity extends BaseActivity {
    private ActivityEditProfileBinding binding;
    private ImagePickerHelper imagePicker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEditProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setupToolbar(binding.toolbar, getString(R.string.edit_profile));

        imagePicker = new ImagePickerHelper(this, () -> this,
                uri -> changeAvatar(done -> AvatarUploader.upload(this, this, uri, done)))
                .enableLink(url -> changeAvatar(done -> AvatarUploader.useLink(this, this, url, done)));
        binding.avatarContainer.setOnClickListener(v -> imagePicker.show());
        binding.tvChangeAvatar.setOnClickListener(v -> imagePicker.show());
        binding.btnSave.setOnClickListener(v -> save());

        User user = SessionManager.get().getUser();
        if (user != null && savedInstanceState == null) {
            setText(binding.tilName, user.getName());
            setText(binding.tilEmail, user.getEmail());
            setText(binding.tilPhone, user.getPhone());
            setText(binding.tilAddress, user.getAddress());
        }
        if (user != null) ImageUtils.loadAvatar(binding.imgAvatar, user.getAvatarUrl());
    }

    private void changeAvatar(Consumer<AvatarUploader.Callback> action) {
        binding.avatarProgress.setVisibility(View.VISIBLE);
        action.accept(user -> {
            binding.avatarProgress.setVisibility(View.GONE);
            if (user != null) {
                ImageUtils.loadAvatar(binding.imgAvatar, user.getAvatarUrl());
                UiUtils.bounce(binding.avatarContainer);
            }
        });
    }

    private void setText(TextInputLayout til, String value) {
        if (til.getEditText() != null) til.getEditText().setText(value);
    }

    private void save() {
        boolean valid = Validator.required(binding.tilName, "Vui lòng nhập họ tên")
                && Validator.phone(binding.tilPhone, true);
        if (!valid) return;

        UiUtils.hideKeyboard(this);
        UiUtils.setLoading(binding.btnSave, true, getString(R.string.action_save));
        Map<String, String> body = new HashMap<>();
        body.put("name", Validator.text(binding.tilName));
        body.put("phone", Validator.text(binding.tilPhone));
        body.put("address", Validator.text(binding.tilAddress));

        ApiClient.get().updateMe(body).enqueue(new ApiCallback<User>(this) {
            @Override
            public void onSuccess(User user) {
                SessionManager.get().saveUser(user);
                UiUtils.toast(EditProfileActivity.this, getString(R.string.profile_updated));
                finish();
            }

            @Override
            public void onError(String message) {
                UiUtils.setLoading(binding.btnSave, false, getString(R.string.action_save));
                UiUtils.toast(EditProfileActivity.this, message);
            }
        });
    }
}
