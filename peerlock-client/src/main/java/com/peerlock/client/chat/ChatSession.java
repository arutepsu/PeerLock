package com.peerlock.client.chat;

import java.io.Closeable;

public interface ChatSession extends Closeable {

    String remoteUsername();

    void sendMessage(ChatMessage message);

    void onMessageReceived(MessageListener listener);

    interface MessageListener {
        void onMessage(ChatMessage message);
    }
}
