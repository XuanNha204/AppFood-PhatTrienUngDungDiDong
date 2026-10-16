package com.example.foodapp.ui.common;

import android.view.View;

import androidx.annotation.DrawableRes;

import com.example.foodapp.databinding.LayoutEmptyStateBinding;

/** Hiển thị trạng thái rỗng / lỗi với icon, tiêu đề, mô tả và nút hành động. */
public final class EmptyState {
    private EmptyState() {}

    public static void show(LayoutEmptyStateBinding b, @DrawableRes int icon, String title, String message,
                            String action, View.OnClickListener onAction) {
        b.emptyIcon.setImageResource(icon);
        b.emptyTitle.setText(title);
        b.emptyMessage.setText(message);
        if (action != null && onAction != null) {
            b.emptyAction.setVisibility(View.VISIBLE);
            b.emptyAction.setText(action);
            b.emptyAction.setOnClickListener(onAction);
        } else {
            b.emptyAction.setVisibility(View.GONE);
        }
        if (b.getRoot().getVisibility() != View.VISIBLE) {
            b.getRoot().setAlpha(0f);
            b.getRoot().setVisibility(View.VISIBLE);
            b.getRoot().animate().alpha(1f).setDuration(300).start();
        }
    }

    public static void hide(LayoutEmptyStateBinding b) {
        b.getRoot().setVisibility(View.GONE);
    }
}
