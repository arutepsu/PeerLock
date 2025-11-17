package com.peerlock.client.chat.history;

import java.util.List;

import com.peerlock.client.chat.ChatMessage;

public interface MessageStore {

    void appendMessage(ChatMessage message);

    List<ChatMessage> getHistoryWith(String otherUsername);

    void clearHistoryWith(String otherUsername);
    
}
