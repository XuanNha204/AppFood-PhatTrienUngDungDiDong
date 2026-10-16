package com.example.foodapp.ui.chat;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodapp.databinding.ItemChatFoodBinding;
import com.example.foodapp.model.Food;
import com.example.foodapp.util.Format;
import com.example.foodapp.util.ImageUtils;

import java.util.List;

/** Thẻ món ăn nằm ngang bên dưới câu trả lời của trợ lý. */
class ChatFoodAdapter extends RecyclerView.Adapter<ChatFoodAdapter.Holder> {
    private final List<Food> foods;
    private final ChatAdapter.Listener listener;

    ChatFoodAdapter(List<Food> foods, ChatAdapter.Listener listener) {
        this.foods = foods;
        this.listener = listener;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Holder(ItemChatFoodBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        Food food = foods.get(position);
        holder.binding.tvName.setText(food.getName());
        holder.binding.tvPrice.setText(Format.money(food.getPrice()));
        ImageUtils.loadFood(holder.binding.imgFood, food.getImageUrl());
        holder.binding.getRoot().setOnClickListener(v -> listener.onFoodClick(food, holder.binding.imgFood));
        holder.binding.btnAdd.setOnClickListener(v -> listener.onAddFood(food, holder.binding.btnAdd));
    }

    @Override
    public int getItemCount() {
        return foods.size();
    }

    static class Holder extends RecyclerView.ViewHolder {
        final ItemChatFoodBinding binding;

        Holder(ItemChatFoodBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
