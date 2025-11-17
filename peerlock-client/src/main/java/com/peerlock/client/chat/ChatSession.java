package com.peerlock.client.chat;

import java.io.Closeable;

public interface ChatSession extends Closeable {

    String remoteUsername();

    void sendMessage(String content);

    /**
     * Register a callback that will be called whenever a message
     * is received on this session.
     */
    void onMessageReceived(MessageListener listener);

    interface MessageListener {
        void onMessage(ChatMessage message);
    }
}
