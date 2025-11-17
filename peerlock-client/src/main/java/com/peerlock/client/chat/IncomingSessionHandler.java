package com.peerlock.client.chat;

@FunctionalInterface
public interface IncomingSessionHandler {
    void onIncomingSession(ChatSession session);
}
