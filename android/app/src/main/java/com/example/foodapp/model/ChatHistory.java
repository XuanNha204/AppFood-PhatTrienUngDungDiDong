package com.example.foodapp.model;

import java.util.ArrayList;
import java.util.List;

public class ChatHistory {
    private List<ChatMessage> messages;

    public List<ChatMessage> getMessages() { return messages == null ? new ArrayList<>() : messages; }
}
