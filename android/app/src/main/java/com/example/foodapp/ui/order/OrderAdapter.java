package com.example.foodapp.ui.order;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodapp.R;
import com.example.foodapp.databinding.ItemOrderBinding;
import com.example.foodapp.model.Order;
import com.example.foodapp.model.OrderItem;
import com.example.foodapp.util.Format;
import com.example.foodapp.util.ImageUtils;

import java.util.ArrayList;
import java.util.List;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.VH> {

    public interface Listener {
        void onOrderClick(Order order);
    }

    private final List<Order> items = new ArrayList<>();
    private final Listener listener;

    public OrderAdapter(Listener listener) {
        this.listener = listener;
    }

    public void setItems(List<Order> orders) {
        items.clear();
        if (orders != null) items.addAll(orders);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new VH(ItemOrderBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        Order order = items.get(position);
        ItemOrderBinding b = holder.binding;
        b.tvCode.setText(b.getRoot().getContext().getString(R.string.order_code, order.getCode()));
        b.tvDate.setText(Format.dateTime(order.getCreatedAt()));
        b.tvTotal.setText(Format.money(order.getTotal()));
        b.tvCount.setText(b.getRoot().getContext().getString(R.string.order_items_count, order.getTotalQuantity()));
        OrderViews.bindStatus(b.tvStatus, order.getStatus());

        StringBuilder names = new StringBuilder();
        for (OrderItem item : order.getItems()) {
            if (names.length() > 0) names.append(", ");
            names.append(item.getName()).append(" ×").append(item.getQuantity());
        }
        b.tvItems.setText(names);
        if (!order.getItems().isEmpty()) ImageUtils.loadFood(b.imgFirst, order.getItems().get(0).getImageUrl());

        b.getRoot().setOnClickListener(v -> listener.onOrderClick(order));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        final ItemOrderBinding binding;

        VH(ItemOrderBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
