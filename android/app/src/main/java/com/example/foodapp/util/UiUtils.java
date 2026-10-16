package com.example.foodapp.util;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.view.animation.OvershootInterpolator;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;

public final class UiUtils {
    private UiUtils() {}

    public static void toast(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    public static void hideKeyboard(Activity activity) {
        View view = activity.getCurrentFocus();
        if (view == null) return;
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    /** Hiệu ứng "nảy" khi bấm (thêm giỏ hàng, yêu thích, ...) */
    public static void bounce(View view) {
        view.animate().cancel();
        view.setScaleX(0.7f);
        view.setScaleY(0.7f);
        view.animate().scaleX(1f).scaleY(1f).setDuration(350)
                .setInterpolator(new OvershootInterpolator(3f)).start();
    }

    /** Bật/tắt trạng thái loading cho nút (đổi chữ + vô hiệu hoá). */
    public static void setLoading(MaterialButton button, boolean loading, String normalText) {
        button.setEnabled(!loading);
        button.setText(loading ? "Vui lòng chờ…" : normalText);
    }

    public static int dp(Context context, int dp) {
        return Math.round(dp * context.getResources().getDisplayMetrics().density);
    }
}
