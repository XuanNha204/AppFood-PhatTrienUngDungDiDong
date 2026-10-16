package com.example.foodapp.ui.chat;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodapp.R;
import com.example.foodapp.databinding.ItemChatBotBinding;
import com.example.foodapp.databinding.ItemChatTypingBinding;
import com.example.foodapp.databinding.ItemChatUserBinding;
import com.example.foodapp.model.ChatMessage;
import com.example.foodapp.model.Food;
import com.example.foodapp.util.UiUtils;

import java.util.ArrayList;
import java.util.List;

class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    interface Listener {
        void onFoodClick(Food food, ImageView image);

        void onAddFood(Food food, View button);

        void onRetry(ChatMessage errorMessage);
    }

    private static final int TYPE_USER = 0;
    private static final int TYPE_BOT = 1;
    private static final int TYPE_TYPING = 2;

    private final List<ChatMessage> messages = new ArrayList<>();
    private final Listener listener;
    private boolean typing;

    ChatAdapter(Listener listener) {
        this.listener = listener;
    }

    void setMessages(List<ChatMessage> data) {
        messages.clear();
        messages.addAll(data);
        typing = false;
        notifyDataSetChanged();
    }

    void add(ChatMessage message) {
        messages.add(message);
        notifyItemInserted(messages.size() - 1);
    }

    void remove(ChatMessage message) {
        int index = messages.indexOf(message);
        if (index < 0) return;
        messages.remove(index);
        notifyItemRemoved(index);
    }

    /** Hiện/ẩn bong bóng "đang gõ" ở cuối danh sách. */
    void setTyping(boolean show) {
        if (typing == show) return;
        typing = show;
        if (show) notifyItemInserted(messages.size());
        else notifyItemRemoved(messages.size());
    }

    boolean hasUserMessages() {
        for (ChatMessage m : messages) if (m.isUser()) return true;
        return false;
    }

    @Override
    public int getItemCount() {
        return messages.size() + (typing ? 1 : 0);
    }

    @Override
    public int getItemViewType(int position) {
        if (position >= messages.size()) return TYPE_TYPING;
        return messages.get(position).isUser() ? TYPE_USER : TYPE_BOT;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == TYPE_USER) return new UserHolder(ItemChatUserBinding.inflate(inflater, parent, false));
        if (viewType == TYPE_TYPING) return new TypingHolder(ItemChatTypingBinding.inflate(inflater, parent, false));
        return new BotHolder(ItemChatBotBinding.inflate(inflater, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof TypingHolder) {
            ((TypingHolder) holder).start();
            return;
        }
        ChatMessage message = messages.get(position);
        if (holder instanceof UserHolder) {
            ((UserHolder) holder).binding.tvMessage.setText(message.getContent());
        } else {
            ((BotHolder) holder).bind(message);
        }
    }

    @Override
    public void onViewRecycled(@NonNull RecyclerView.ViewHolder holder) {
        if (holder instanceof TypingHolder) ((TypingHolder) holder).stop();
        super.onViewRecycled(holder);
    }

    @Override
    public void onViewDetachedFromWindow(@NonNull RecyclerView.ViewHolder holder) {
        if (holder instanceof TypingHolder) ((TypingHolder) holder).stop();
        super.onViewDetachedFromWindow(holder);
    }

    static class UserHolder extends RecyclerView.ViewHolder {
        final ItemChatUserBinding binding;

        UserHolder(ItemChatUserBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    class BotHolder extends RecyclerView.ViewHolder {
        final ItemChatBotBinding binding;

        BotHolder(ItemChatBotBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
            binding.rvFoods.setLayoutManager(
                    new LinearLayoutManager(binding.getRoot().getContext(), LinearLayoutManager.HORIZONTAL, false));
        }

        void bind(ChatMessage message) {
            binding.tvMessage.setText(ChatText.format(message.getContent()));
            binding.tvMessage.setTextColor(binding.getRoot().getContext().getColor(
                    message.isError() ? R.color.danger : R.color.text_primary));

            binding.btnRetry.setVisibility(message.isError() ? View.VISIBLE : View.GONE);
            binding.btnRetry.setOnClickListener(v -> listener.onRetry(message));

            List<Food> foods = message.getFoods();
            binding.rvFoods.setVisibility(foods.isEmpty() ? View.GONE : View.VISIBLE);
            binding.rvFoods.setAdapter(foods.isEmpty() ? null : new ChatFoodAdapter(foods, listener));
        }
    }

    /** 3 chấm nhảy lần lượt như đang gõ phím. */
    static class TypingHolder extends RecyclerView.ViewHolder {
        private final ItemChatTypingBinding binding;
        private final List<ObjectAnimator> animators = new ArrayList<>();

        TypingHolder(ItemChatTypingBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void start() {
            stop();
            float jump = -UiUtils.dp(binding.getRoot().getContext(), 5);
            for (int i = 0; i < binding.dots.getChildCount(); i++) {
                ObjectAnimator a = ObjectAnimator.ofFloat(binding.dots.getChildAt(i), View.TRANSLATION_Y, 0f, jump, 0f);
                a.setDuration(600);
                a.setStartDelay(i * 150L);
                a.setRepeatCount(ValueAnimator.INFINITE);
                a.start();
                animators.add(a);
            }
        }

        void stop() {
            for (ObjectAnimator a : animators) a.cancel();
            animators.clear();
            for (int i = 0; i < binding.dots.getChildCount(); i++) {
                binding.dots.getChildAt(i).setTranslationY(0f);
            }
        }
    }
}
