package com.peerlock.client.chat;

import java.io.IOException;
import java.net.Socket;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.peerlock.client.chat.history.MessageStore;
import com.peerlock.common.dto.PeerInfoResponse;

public class ChatManager {

    private final String localUsername;
    private final ChatServer chatServer;
    private final Map<String, ChatSession> sessions = new ConcurrentHashMap<>();
    private final MessageStore messageStore;

    public ChatManager(String localUsername, int listenPort, MessageStore messageStore) {
        this.localUsername = localUsername;
        this.messageStore = messageStore;
        this.chatServer = new ChatServer(localUsername, listenPort, this::handleIncomingSession);
        this.chatServer.start();
    }

    private void handleIncomingSession(ChatSession session) {
        sessions.put(session.remoteUsername(), session);
        session.onMessageReceived(msg -> messageStore.appendMessage(msg));
    }

    /**
     * Open an outgoing chat to the given peer.
     */
    public ChatSession openChat(PeerInfoResponse peer) throws IOException {
        Socket socket = new Socket(peer.host(), peer.port());
        ChatHandshake.Result result =
                ChatHandshake.performClientHandshake(socket, localUsername, "peerlock-client-0.1");
        String remoteUsername = result.remoteUsername();

        ChatSession session = new TcpChatSession(socket, localUsername, remoteUsername);
        session.onMessageReceived(msg -> messageStore.appendMessage(msg));
        sessions.put(remoteUsername, session);
        return session;
    }

    public ChatSession getSessionWith(String username) {
        return sessions.get(username);
    }

    public Collection<ChatSession> getAllSessions() {
        return sessions.values();
    }

    public void closeSession(String username) {
        ChatSession session = sessions.remove(username);
        if (session != null) {
            try {
                session.close();
            } catch (IOException ignored) {}
        }
    }

    public void shutdown() {
        try {
            chatServer.close();
        } catch (Exception ignored) {}

        sessions.values().forEach(s -> {
            try { s.close(); } catch (IOException ignored) {}
        });
        sessions.clear();
    }

    
    public void sendMessage(String toUser, String content) {
        ChatSession session = sessions.get(toUser);
        if (session == null) throw new IllegalStateException("No session with " + toUser);

        ChatMessage msg = new ChatMessage(localUsername, toUser, Instant.now(), content);
        session.sendMessage(content);
        messageStore.appendMessage(msg);
    }

    public List<ChatMessage> getHistoryWith(String otherUsername) {
        return messageStore.getHistoryWith(otherUsername);
    }
}
