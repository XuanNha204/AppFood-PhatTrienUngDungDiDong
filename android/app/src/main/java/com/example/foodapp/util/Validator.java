package com.example.foodapp.util;

import android.util.Patterns;

import com.google.android.material.textfield.TextInputLayout;

public final class Validator {
    private Validator() {}

    public static String text(TextInputLayout layout) {
        return layout.getEditText() == null ? "" : layout.getEditText().getText().toString().trim();
    }

    public static boolean required(TextInputLayout layout, String message) {
        if (text(layout).isEmpty()) return fail(layout, message);
        return ok(layout);
    }

    public static boolean email(TextInputLayout layout) {
        String value = text(layout);
        if (value.isEmpty()) return fail(layout, "Vui lòng nhập email");
        if (!Patterns.EMAIL_ADDRESS.matcher(value).matches()) return fail(layout, "Email không hợp lệ");
        return ok(layout);
    }

    public static boolean password(TextInputLayout layout) {
        String value = text(layout);
        if (value.isEmpty()) return fail(layout, "Vui lòng nhập mật khẩu");
        if (value.length() < 6) return fail(layout, "Mật khẩu tối thiểu 6 ký tự");
        return ok(layout);
    }

    public static boolean matches(TextInputLayout confirm, TextInputLayout password) {
        if (!text(confirm).equals(text(password))) return fail(confirm, "Mật khẩu nhập lại không khớp");
        return ok(confirm);
    }

    /** Số điện thoại VN: 10 số, bắt đầu bằng 0. allowEmpty = true nếu không bắt buộc. */
    public static boolean phone(TextInputLayout layout, boolean allowEmpty) {
        String value = text(layout);
        if (value.isEmpty()) return allowEmpty ? ok(layout) : fail(layout, "Vui lòng nhập số điện thoại");
        if (!value.matches("^0\\d{9}$")) return fail(layout, "Số điện thoại không hợp lệ (10 số)");
        return ok(layout);
    }

    private static boolean fail(TextInputLayout layout, String message) {
        layout.setError(message);
        layout.requestFocus();
        return false;
    }

    private static boolean ok(TextInputLayout layout) {
        layout.setError(null);
        layout.setErrorEnabled(false);
        return true;
    }
}
