package com.peerlock.client.chat;

import java.io.Closeable;

import com.peerlock.common.dto.ChatMessageDto;

public interface ChatSession extends Closeable {

    String remoteUsername();

    void sendMessage(ChatMessageDto message);

    void onMessageReceived(MessageListener listener);

    interface MessageListener {
        void onMessage(ChatMessageDto message);
    }
}
