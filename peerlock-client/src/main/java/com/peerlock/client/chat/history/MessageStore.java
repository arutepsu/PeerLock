package com.peerlock.client.chat.history;

import java.util.List;

import com.peerlock.common.dto.ChatMessageDto;

public interface MessageStore {

    void appendMessage(ChatMessageDto message);

    List<ChatMessageDto> getHistoryWith(String otherUsername);

    void clearHistoryWith(String otherUsername);
    
}
