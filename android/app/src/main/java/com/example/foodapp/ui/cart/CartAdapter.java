package com.example.foodapp.ui.cart;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodapp.databinding.ItemCartBinding;
import com.example.foodapp.model.CartItem;
import com.example.foodapp.util.Format;
import com.example.foodapp.util.ImageUtils;
import com.example.foodapp.util.UiUtils;

import java.util.ArrayList;
import java.util.List;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.VH> {

    public interface Listener {
        void onQuantityChanged(CartItem item, int newQuantity);

        void onItemClick(CartItem item);
    }

    private final List<CartItem> items = new ArrayList<>();
    private final Listener listener;

    public CartAdapter(Listener listener) {
        this.listener = listener;
    }

    public void setItems(List<CartItem> list) {
        items.clear();
        if (list != null) items.addAll(list);
        notifyDataSetChanged();
    }

    public CartItem getItem(int position) {
        return items.get(position);
    }

    public CartItem removeAt(int position) {
        CartItem removed = items.remove(position);
        notifyItemRemoved(position);
        return removed;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new VH(ItemCartBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        CartItem item = items.get(position);
        ItemCartBinding b = holder.binding;
        b.tvName.setText(item.getFood().getName());
        b.tvUnitPrice.setText(Format.money(item.getFood().getPrice()) + " / phần");
        b.tvQuantity.setText(String.valueOf(item.getQuantity()));
        b.tvLineTotal.setText(Format.money(item.getLineTotal()));
        ImageUtils.loadFood(b.imgFood, item.getFood().getImageUrl());

        b.btnPlus.setOnClickListener(v -> {
            if (item.getQuantity() >= 99) return;
            UiUtils.bounce(b.tvQuantity);
            listener.onQuantityChanged(item, item.getQuantity() + 1);
        });
        b.btnMinus.setOnClickListener(v -> {
            UiUtils.bounce(b.tvQuantity);
            listener.onQuantityChanged(item, item.getQuantity() - 1);
        });
        b.getRoot().setOnClickListener(v -> listener.onItemClick(item));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        final ItemCartBinding binding;

        VH(ItemCartBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
