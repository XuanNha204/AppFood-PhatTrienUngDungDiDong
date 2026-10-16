package com.example.foodapp.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.view.animation.AnimationUtils;

import androidx.appcompat.app.AppCompatActivity;

import com.example.foodapp.R;
import com.example.foodapp.api.ApiCallback;
import com.example.foodapp.api.ApiClient;
import com.example.foodapp.databinding.ActivityForgotPasswordBinding;
import com.example.foodapp.model.MessageResponse;
import com.example.foodapp.util.UiUtils;
import com.example.foodapp.util.Validator;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.HashMap;
import java.util.Map;

/** Quên mật khẩu 2 bước: (1) nhập email nhận OTP, (2) nhập OTP + mật khẩu mới. */
public class ForgotPasswordActivity extends AppCompatActivity {
    private ActivityForgotPasswordBinding binding;
    private CountDownTimer resendTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityForgotPasswordBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.toolbar.setNavigationIcon(R.drawable.ic_arrow_back);
        binding.toolbar.setNavigationOnClickListener(v -> finish());

        String email = getIntent().getStringExtra(LoginActivity.EXTRA_EMAIL);
        if (email != null && binding.tilEmail.getEditText() != null) {
            binding.tilEmail.getEditText().setText(email);
        }

        binding.btnSendOtp.setOnClickListener(v -> sendOtp());
        binding.tvResend.setOnClickListener(v -> sendOtp());
        binding.btnReset.setOnClickListener(v -> resetPassword());
    }

    private void sendOtp() {
        if (!Validator.email(binding.tilEmail)) return;
        UiUtils.hideKeyboard(this);
        UiUtils.setLoading(binding.btnSendOtp, true, getString(R.string.action_send_otp));
        binding.tvResend.setEnabled(false);

        Map<String, String> body = new HashMap<>();
        body.put("email", Validator.text(binding.tilEmail));
        ApiClient.get().forgotPassword(body).enqueue(new ApiCallback<MessageResponse>(this) {
            @Override
            public void onSuccess(MessageResponse data) {
                showResetStep();
                startResendCountdown();
                if (data.getDevOtp() != null) {
                    // Backend chưa cấu hình email -> hiển thị OTP để demo
                    if (binding.tilOtp.getEditText() != null) binding.tilOtp.getEditText().setText(data.getDevOtp());
                    new MaterialAlertDialogBuilder(ForgotPasswordActivity.this)
                            .setTitle("Chế độ demo")
                            .setMessage("Backend chưa cấu hình gửi email.\nMã OTP của bạn là: " + data.getDevOtp())
                            .setPositiveButton(R.string.ok, null)
                            .show();
                } else {
                    UiUtils.toast(ForgotPasswordActivity.this, data.getMessage());
                }
            }

            @Override
            public void onError(String message) {
                binding.tilEmail.setError(message);
                binding.tvResend.setEnabled(true);
            }

            @Override
            public void onComplete() {
                UiUtils.setLoading(binding.btnSendOtp, false, getString(R.string.action_send_otp));
            }
        });
    }

    private void showResetStep() {
        if (binding.stepReset.getVisibility() == View.VISIBLE) return;
        binding.tvDescription.setText(R.string.forgot_step2);
        binding.btnSendOtp.setVisibility(View.GONE);
        binding.tilEmail.setEnabled(false);
        binding.imgIcon.setImageResource(R.drawable.ic_email);
        binding.stepReset.setVisibility(View.VISIBLE);
        binding.stepReset.startAnimation(AnimationUtils.loadAnimation(this, R.anim.fade_in_up));
    }

    private void startResendCountdown() {
        if (resendTimer != null) resendTimer.cancel();
        binding.tvResend.setEnabled(false);
        resendTimer = new CountDownTimer(60_000, 1_000) {
            @Override
            public void onTick(long millisUntilFinished) {
                binding.tvResend.setText(getString(R.string.resend_in, (int) (millisUntilFinished / 1000)));
            }

            @Override
            public void onFinish() {
                binding.tvResend.setText(R.string.action_resend_otp);
                binding.tvResend.setEnabled(true);
            }
        }.start();
    }

    private void resetPassword() {
        String otp = Validator.text(binding.tilOtp);
        if (otp.length() != 6) {
            binding.tilOtp.setError("Mã OTP gồm 6 chữ số");
            return;
        }
        binding.tilOtp.setError(null);
        if (!Validator.password(binding.tilNewPassword)
                || !Validator.matches(binding.tilConfirm, binding.tilNewPassword)) return;

        UiUtils.hideKeyboard(this);
        UiUtils.setLoading(binding.btnReset, true, getString(R.string.action_reset_password));
        String email = Validator.text(binding.tilEmail);
        Map<String, String> body = new HashMap<>();
        body.put("email", email);
        body.put("otp", otp);
        body.put("newPassword", Validator.text(binding.tilNewPassword));
        ApiClient.get().resetPassword(body).enqueue(new ApiCallback<MessageResponse>(this) {
            @Override
            public void onSuccess(MessageResponse data) {
                new MaterialAlertDialogBuilder(ForgotPasswordActivity.this)
                        .setIcon(R.drawable.ic_check_circle)
                        .setTitle("Thành công")
                        .setMessage(data.getMessage())
                        .setCancelable(false)
                        .setPositiveButton(R.string.action_login, (d, w) -> {
                            Intent intent = new Intent(ForgotPasswordActivity.this, LoginActivity.class);
                            intent.putExtra(LoginActivity.EXTRA_EMAIL, email);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK
                                    | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            finish();
                        })
                        .show();
            }

            @Override
            public void onError(String message) {
                UiUtils.setLoading(binding.btnReset, false, getString(R.string.action_reset_password));
                binding.tilOtp.setError(message);
            }
        });
    }

    @Override
    protected void onDestroy() {
        if (resendTimer != null) resendTimer.cancel();
        super.onDestroy();
    }
}
