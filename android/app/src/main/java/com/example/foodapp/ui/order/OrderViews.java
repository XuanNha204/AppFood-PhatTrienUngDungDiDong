package com.example.foodapp.ui.order;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.example.foodapp.R;
import com.example.foodapp.databinding.ItemOrderLineBinding;
import com.example.foodapp.model.Order;
import com.example.foodapp.util.Format;
import com.example.foodapp.util.ImageUtils;

/** Hàm hiển thị dùng chung cho đơn hàng: trạng thái (màu + chữ) và danh sách món. */
public final class OrderViews {
    private OrderViews() {}

    public static void bindStatus(TextView view, String status) {
        Context c = view.getContext();
        int text;
        int color;
        if (Order.CONFIRMED.equals(status)) {
            text = R.string.status_confirmed;
            color = R.color.info;
        } else if (Order.DELIVERING.equals(status)) {
            text = R.string.status_delivering;
            color = R.color.purple;
        } else if (Order.COMPLETED.equals(status)) {
            text = R.string.status_completed;
            color = R.color.success;
        } else if (Order.CANCELLED.equals(status)) {
            text = R.string.status_cancelled;
            color = R.color.danger;
        } else {
            text = R.string.status_pending;
            color = R.color.primary;
        }
        int resolved = ContextCompat.getColor(c, color);
        view.setText(text);
        view.setTextColor(resolved);
        view.setBackgroundResource(R.drawable.bg_status_chip);
        // Nền cùng màu với chữ nhưng nhạt (alpha ~15%)
        view.setBackgroundTintList(ColorStateList.valueOf((resolved & 0x00FFFFFF) | 0x26000000));
    }

    public static void bindItems(LinearLayout container, Iterable<Line> lines) {
        container.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(container.getContext());
        for (Line line : lines) {
            ItemOrderLineBinding b = ItemOrderLineBinding.inflate(inflater, container, false);
            b.tvName.setText(line.name);
            b.tvQuantity.setText(Format.money(line.price) + " × " + line.quantity);
            b.tvLineTotal.setText(Format.money(line.price * line.quantity));
            ImageUtils.loadFood(b.imgFood, line.imageUrl);
            container.addView(b.getRoot());
        }
    }

    /** Dữ liệu tối thiểu của một dòng món (dùng cho cả CartItem và OrderItem). */
    public static class Line {
        final String name;
        final String imageUrl;
        final long price;
        final int quantity;

        public Line(String name, String imageUrl, long price, int quantity) {
            this.name = name;
            this.imageUrl = imageUrl;
            this.price = price;
            this.quantity = quantity;
        }
    }
}
