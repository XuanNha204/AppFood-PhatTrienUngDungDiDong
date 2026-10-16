package com.example.foodapp.model;

import java.util.ArrayList;
import java.util.List;

public class ChatMessage {
    public static final String ROLE_USER = "user";
    public static final String ROLE_ASSISTANT = "assistant";

    private String role;
    private String content;
    private List<Food> foods;
    private String createdAt;

    // Chỉ dùng trên app: tin nhắn báo lỗi kèm nút "Gửi lại" câu hỏi retryText
    private transient boolean error;
    private transient String retryText;

    public ChatMessage() {}

    public static ChatMessage user(String text) {
        ChatMessage m = new ChatMessage();
        m.role = ROLE_USER;
        m.content = text;
        return m;
    }

    public static ChatMessage assistant(String text) {
        ChatMessage m = new ChatMessage();
        m.role = ROLE_ASSISTANT;
        m.content = text;
        return m;
    }

    public static ChatMessage error(String message, String retryText) {
        ChatMessage m = assistant(message);
        m.error = true;
        m.retryText = retryText;
        return m;
    }

    public boolean isUser() { return ROLE_USER.equals(role); }
    public String getContent() { return content == null ? "" : content; }
    public List<Food> getFoods() { return foods == null ? new ArrayList<>() : foods; }
    public String getCreatedAt() { return createdAt; }
    public boolean isError() { return error; }
    public String getRetryText() { return retryText; }
}
