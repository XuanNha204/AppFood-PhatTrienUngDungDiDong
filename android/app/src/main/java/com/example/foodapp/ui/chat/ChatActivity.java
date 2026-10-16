package com.example.foodapp.ui.chat;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.ImageView;

import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.foodapp.R;
import com.example.foodapp.api.ApiCallback;
import com.example.foodapp.api.ApiClient;
import com.example.foodapp.data.FoodActions;
import com.example.foodapp.data.SessionManager;
import com.example.foodapp.databinding.ActivityChatBinding;
import com.example.foodapp.model.ChatHistory;
import com.example.foodapp.model.ChatMessage;
import com.example.foodapp.model.Food;
import com.example.foodapp.model.MessageResponse;
import com.example.foodapp.model.User;
import com.example.foodapp.ui.common.BaseActivity;
import com.example.foodapp.ui.food.FoodDetailActivity;
import com.example.foodapp.util.UiUtils;
import com.google.android.material.chip.Chip;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Trò chuyện với trợ lý AI: gợi ý món theo khẩu vị, ngân sách, calo; thẻ món bấm để xem/thêm vào giỏ. */
public class ChatActivity extends BaseActivity implements ChatAdapter.Listener {
    private ActivityChatBinding binding;
    private ChatAdapter adapter;
    private boolean sending;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupToolbar(binding.toolbar, "");
        binding.toolbar.inflateMenu(R.menu.menu_chat);
        binding.toolbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_clear_chat) {
                confirmClear();
                return true;
            }
            return false;
        });

        adapter = new ChatAdapter(this);
        LinearLayoutManager lm = new LinearLayoutManager(this);
        lm.setStackFromEnd(true);
        binding.rvMessages.setLayoutManager(lm);
        binding.rvMessages.setAdapter(adapter);
        // Bàn phím mở làm danh sách co lại -> giữ tin nhắn mới nhất trong tầm nhìn
        binding.rvMessages.addOnLayoutChangeListener((v, l, t, r, b, ol, ot, or, ob) -> {
            if (b < ob) scrollToBottom();
        });

        setupInput();
        setupSuggestions();
        loadHistory();
    }

    private void setupInput() {
        binding.etMessage.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                updateSendButton();
            }
        });
        binding.etMessage.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                sendFromInput();
                return true;
            }
            return false;
        });
        binding.btnSend.setOnClickListener(v -> sendFromInput());
    }

    private void setupSuggestions() {
        for (String suggestion : getResources().getStringArray(R.array.chat_suggestions)) {
            Chip chip = new Chip(this);
            chip.setText(suggestion);
            chip.setChipBackgroundColorResource(R.color.primary_light);
            chip.setTextColor(getColor(R.color.primary_dark));
            chip.setChipStrokeWidth(0f);
            // Bỏ emoji ở đầu khi gửi cho AI
            String question = suggestion.contains(" ") ? suggestion.substring(suggestion.indexOf(' ') + 1) : suggestion;
            chip.setOnClickListener(v -> send(question, true));
            binding.chipSuggestions.addView(chip);
        }
    }

    private void updateSendButton() {
        boolean hasText = binding.etMessage.getText() != null
                && !binding.etMessage.getText().toString().trim().isEmpty();
        binding.btnSend.setEnabled(hasText && !sending);
    }

    private void loadHistory() {
        binding.progress.setVisibility(View.VISIBLE);
        ApiClient.get().getChatHistory().enqueue(new ApiCallback<ChatHistory>(this) {
            @Override
            public void onSuccess(ChatHistory data) {
                showMessages(data.getMessages());
            }

            @Override
            public void onError(String message) {
                showMessages(Collections.emptyList());
                UiUtils.toast(ChatActivity.this, message);
            }

            @Override
            public void onComplete() {
                binding.progress.setVisibility(View.GONE);
            }
        });
    }

    /** Lời chào luôn đứng đầu (chỉ hiển thị trên app, không lưu trên server). */
    private void showMessages(List<ChatMessage> history) {
        List<ChatMessage> all = new ArrayList<>();
        all.add(ChatMessage.assistant(getString(R.string.chat_welcome, firstName())));
        all.addAll(history);
        adapter.setMessages(all);
        scrollToBottom();
    }

    private String firstName() {
        User user = SessionManager.get().getUser();
        if (user == null || user.getName() == null || user.getName().trim().isEmpty()) return "bạn";
        String[] parts = user.getName().trim().split("\\s+");
        return parts[parts.length - 1];
    }

    private void sendFromInput() {
        if (binding.etMessage.getText() == null) return;
        String text = binding.etMessage.getText().toString().trim();
        if (text.isEmpty() || sending) return;
        binding.etMessage.setText("");
        send(text, true);
    }

    private void send(String text, boolean showUserBubble) {
        if (sending) return;
        setSending(true);
        if (showUserBubble) adapter.add(ChatMessage.user(text));
        adapter.setTyping(true);
        scrollToBottom();

        ApiClient.get().sendChatMessage(Collections.singletonMap("message", text))
                .enqueue(new ApiCallback<ChatMessage>(this) {
                    @Override
                    public void onSuccess(ChatMessage reply) {
                        adapter.setTyping(false);
                        adapter.add(reply);
                    }

                    @Override
                    public void onError(String message) {
                        adapter.setTyping(false);
                        adapter.add(ChatMessage.error(message, text));
                    }

                    @Override
                    public void onComplete() {
                        setSending(false);
                        scrollToBottom();
                    }
                });
    }

    private void setSending(boolean value) {
        sending = value;
        binding.tvStatus.setText(value ? R.string.chat_thinking : R.string.chat_subtitle);
        binding.chipSuggestions.setEnabled(!value);
        for (int i = 0; i < binding.chipSuggestions.getChildCount(); i++) {
            binding.chipSuggestions.getChildAt(i).setEnabled(!value);
        }
        updateSendButton();
    }

    private void scrollToBottom() {
        binding.rvMessages.post(() -> {
            int count = adapter.getItemCount();
            if (count > 0) binding.rvMessages.smoothScrollToPosition(count - 1);
        });
    }

    private void confirmClear() {
        if (!adapter.hasUserMessages()) return;
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.chat_clear)
                .setMessage(R.string.chat_clear_confirm)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.chat_clear, (d, w) -> clearChat())
                .show();
    }

    private void clearChat() {
        ApiClient.get().clearChat().enqueue(new ApiCallback<MessageResponse>(this) {
            @Override
            public void onSuccess(MessageResponse data) {
                showMessages(Collections.emptyList());
                UiUtils.toast(ChatActivity.this, data.getMessage());
            }

            @Override
            public void onError(String message) {
                UiUtils.toast(ChatActivity.this, message);
            }
        });
    }

    // ===== ChatAdapter.Listener =====

    @Override
    public void onFoodClick(Food food, ImageView image) {
        FoodDetailActivity.start(this, food, image);
    }

    @Override
    public void onAddFood(Food food, View button) {
        UiUtils.bounce(button);
        FoodActions.addToCart(this, this, food, 1, cart -> {
            if (cart != null) UiUtils.toast(this, getString(R.string.chat_added, food.getName()));
        });
    }

    @Override
    public void onRetry(ChatMessage errorMessage) {
        if (sending) return;
        adapter.remove(errorMessage);
        send(errorMessage.getRetryText(), false);
    }
}
