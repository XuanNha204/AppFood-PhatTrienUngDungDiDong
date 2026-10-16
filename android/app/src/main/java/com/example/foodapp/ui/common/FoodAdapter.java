package com.example.foodapp.ui.common;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodapp.R;
import com.example.foodapp.data.AppState;
import com.example.foodapp.databinding.ItemFoodBinding;
import com.example.foodapp.model.Food;
import com.example.foodapp.util.Format;
import com.example.foodapp.util.ImageUtils;

import java.util.ArrayList;
import java.util.List;

/** Adapter dùng chung cho mọi danh sách món ăn (lưới 2 cột hoặc cuộn ngang). */
public class FoodAdapter extends RecyclerView.Adapter<FoodAdapter.VH> {

    public interface Listener {
        void onFoodClick(Food food, ImageView sharedImage);

        void onAddToCart(Food food, View button);

        void onToggleFavorite(Food food, int position);
    }

    private final List<Food> items = new ArrayList<>();
    private final Listener listener;
    private final int fixedWidthPx;

    /** @param fixedWidthPx > 0 khi dùng trong danh sách cuộn ngang */
    public FoodAdapter(Listener listener, int fixedWidthPx) {
        this.listener = listener;
        this.fixedWidthPx = fixedWidthPx;
    }

    public FoodAdapter(Listener listener) {
        this(listener, 0);
    }

    public void setItems(List<Food> foods) {
        items.clear();
        if (foods != null) items.addAll(foods);
        notifyDataSetChanged();
    }

    public List<Food> getItems() {
        return items;
    }

    public void removeAt(int position) {
        if (position < 0 || position >= items.size()) return;
        items.remove(position);
        notifyItemRemoved(position);
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemFoodBinding binding = ItemFoodBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        if (fixedWidthPx > 0) {
            ViewGroup.LayoutParams lp = binding.getRoot().getLayoutParams();
            lp.width = fixedWidthPx;
            binding.getRoot().setLayoutParams(lp);
        }
        return new VH(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        Food food = items.get(position);
        ItemFoodBinding b = holder.binding;

        b.tvName.setText(food.getName());
        b.tvCategory.setText(food.getCategoryName());
        b.tvRating.setText(Format.rating(food.getRating()));
        b.tvPrepTime.setText("• " + food.getPrepTime() + " phút");
        b.tvPrice.setText(Format.money(food.getPrice()));
        b.tvPopular.setVisibility(food.isPopular() ? View.VISIBLE : View.GONE);
        b.btnFavorite.setImageResource(AppState.isFavorite(food.getId())
                ? R.drawable.ic_favorite : R.drawable.ic_favorite_border);
        b.imgFood.setTransitionName("food_image_" + food.getId());
        ImageUtils.loadFood(b.imgFood, food.getImageUrl());

        b.getRoot().setOnClickListener(v -> listener.onFoodClick(food, b.imgFood));
        b.btnAdd.setOnClickListener(v -> listener.onAddToCart(food, v));
        b.btnFavorite.setOnClickListener(v -> {
            int pos = holder.getBindingAdapterPosition();
            if (pos != RecyclerView.NO_POSITION) listener.onToggleFavorite(food, pos);
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        final ItemFoodBinding binding;

        VH(ItemFoodBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
