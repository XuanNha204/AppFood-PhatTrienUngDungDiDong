package com.example.foodapp.ui.profile;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;

import com.example.foodapp.BuildConfig;
import com.example.foodapp.R;
import com.example.foodapp.api.ApiCallback;
import com.example.foodapp.api.ApiClient;
import com.example.foodapp.data.AppState;
import com.example.foodapp.data.SessionManager;
import com.example.foodapp.databinding.FragmentProfileBinding;
import com.example.foodapp.model.User;
import com.example.foodapp.ui.auth.LoginActivity;
import com.example.foodapp.ui.common.ImagePickerHelper;
import com.example.foodapp.ui.food.AddEditFoodActivity;
import com.example.foodapp.ui.food.FoodListActivity;
import com.example.foodapp.ui.main.TabFragment;
import com.example.foodapp.ui.order.OrdersActivity;
import com.example.foodapp.util.ImageUtils;
import com.example.foodapp.util.UiUtils;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.function.Consumer;

public class ProfileFragment extends Fragment implements TabFragment {
    private FragmentProfileBinding binding;
    private ImagePickerHelper imagePicker;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Phải đăng ký trước khi Fragment STARTED
        imagePicker = new ImagePickerHelper(this, this::requireContext,
                uri -> changeAvatar(done -> AvatarUploader.upload(requireContext(), this, uri, done)))
                .enableLink(url -> changeAvatar(done -> AvatarUploader.useLink(requireContext(), this, url, done)));
    }

    private void changeAvatar(Consumer<AvatarUploader.Callback> action) {
        if (binding == null) return;
        binding.avatarProgress.setVisibility(View.VISIBLE);
        action.accept(user -> {
            if (binding == null) return;
            binding.avatarProgress.setVisibility(View.GONE);
            if (user != null) {
                bindUser(user);
                UiUtils.bounce(binding.avatarContainer);
            }
        });
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.avatarContainer.setOnClickListener(v -> imagePicker.show());
        binding.menuEditProfile.setOnClickListener(v ->
                startActivity(new Intent(requireContext(), EditProfileActivity.class)));
        binding.menuOrders.setOnClickListener(v ->
                startActivity(new Intent(requireContext(), OrdersActivity.class)));
        binding.menuMyFoods.setOnClickListener(v -> FoodListActivity.startMine(requireContext()));
        binding.menuAddFood.setOnClickListener(v -> AddEditFoodActivity.startCreate(requireContext(), null));
        binding.menuChangePassword.setOnClickListener(v ->
                startActivity(new Intent(requireContext(), ChangePasswordActivity.class)));
        binding.btnLogout.setOnClickListener(v -> confirmLogout());

        binding.switchDarkMode.setChecked(SessionManager.get().isDarkMode());
        binding.switchDarkMode.setOnCheckedChangeListener((button, checked) -> {
            SessionManager.get().setDarkMode(checked);
            AppCompatDelegate.setDefaultNightMode(checked
                    ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);
        });
        binding.tvVersion.setText("Food App v" + BuildConfig.VERSION_NAME);

        User user = SessionManager.get().getUser();
        if (user != null) bindUser(user);
        refreshUser();
    }

    private void bindUser(User user) {
        binding.tvName.setText(user.getName());
        binding.tvEmail.setText(user.getEmail());
        binding.tvPhone.setText(user.getPhone());
        binding.tvPhone.setVisibility(user.getPhone().isEmpty() ? View.GONE : View.VISIBLE);
        ImageUtils.loadAvatar(binding.imgAvatar, user.getAvatarUrl());
    }

    private void refreshUser() {
        ApiClient.get().getMe().enqueue(new ApiCallback<User>(this) {
            @Override
            public void onSuccess(User user) {
                SessionManager.get().saveUser(user);
                bindUser(user);
            }

            @Override
            public void onError(String message) {
                // Giữ thông tin đã lưu
            }
        });
    }

    private void confirmLogout() {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.logout)
                .setMessage(R.string.confirm_logout)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.logout, (d, w) -> logout())
                .show();
    }

    private void logout() {
        SessionManager.get().clear();
        AppState.reset();
        Intent intent = new Intent(requireContext(), LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        requireActivity().finish();
    }

    @Override
    public void onTabSelected() {
        User user = SessionManager.get().getUser();
        if (user != null) bindUser(user);
        refreshUser();
    }

    @Override
    public void onResume() {
        super.onResume();
        // Cập nhật sau khi quay lại từ màn Chỉnh sửa hồ sơ
        User user = SessionManager.get().getUser();
        if (user != null && !isHidden()) bindUser(user);
    }

    @Override
    public void onDestroyView() {
        binding = null;
        super.onDestroyView();
    }
}
