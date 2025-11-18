package com.peerlock.client.chat;

import java.io.IOException;
import java.net.Socket;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.crypto.SecretKey;

import com.peerlock.client.chat.history.MessageHistorySyncService;
import com.peerlock.client.chat.history.MessageStore;
import com.peerlock.common.dto.ChatMessageDto;
import com.peerlock.common.dto.PeerInfoResponse;


public class ChatManager {

    private final String localUsername;
    private final ChatServer chatServer;
    private final Map<String, ChatSession> sessions = new ConcurrentHashMap<>();
    private final MessageStore messageStore;
    private final MessageHistorySyncService historySyncService;

    public ChatManager(String localUsername,
                       int listenPort,
                       MessageStore messageStore,
                       MessageHistorySyncService historySyncService) {
        this.localUsername = localUsername;
        this.messageStore = messageStore;
        this.historySyncService = historySyncService;

        this.chatServer = new ChatServer(
                localUsername,
                listenPort,
                messageStore,
                historySyncService,
                this::handleIncomingSession
        );
        this.chatServer.start();
    }

    private void handleIncomingSession(ChatSession session) {
        sessions.put(session.remoteUsername(), session);

        // TcpChatSession already stores + syncs messages,
        // so here we only propagate to UI / observers later.
        session.onMessageReceived(msg -> {
            // messageStore.appendMessage(msg)
            // TODO: notify UI / observers
        });
    }

    /**
     * Open an outgoing chat to the given peer.
     */
    public ChatSession openChat(PeerInfoResponse peer) throws IOException {
        Socket socket = new Socket(peer.host(), peer.port());

        ChatHandshake.Result result =
                ChatHandshake.performClientHandshake(socket, localUsername, "peerlock-client-0.1");

        SecretKey key = result.sharedKey();
        String remoteUsername = result.remoteUsername();

        ChatSession session = new TcpChatSession(
                socket,
                localUsername,
                remoteUsername,
                key,
                messageStore,
                historySyncService
        );

        session.onMessageReceived(msg -> {
            // messageStore.appendMessage(msg);
            // TODO: notify UI / observers
        });

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

    /**
     * Send a message to an existing session.
     */
    public void sendMessage(String toUser, String content) {
        ChatSession session = sessions.get(toUser);
        if (session == null) {
            throw new IllegalStateException("No session with " + toUser);
        }

        ChatMessageDto msg = new ChatMessageDto(
                localUsername,
                toUser,
                content,
                Instant.now()
        );

        // ❌ no local append here – TcpChatSession.sendMessage handles:
        //    - store in MessageStore
        //    - push to MessageHistorySyncService
        session.sendMessage(msg);
    }

    /**
     * Get history with a given user, synced with server.
     */
    public List<ChatMessageDto> getHistoryWith(String otherUsername) {
        // instead of only local messageStore.getHistoryWith(...)
        return historySyncService.loadAndSync(otherUsername);
    }
}