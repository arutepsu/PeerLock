package com.peerlock.client.chat;

import com.peerlock.common.dto.PeerInfoResponse;

import java.io.IOException;
import java.net.Socket;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ChatManager {

    private final String localUsername;
    private final ChatServer chatServer;
    private final Map<String, ChatSession> sessions = new ConcurrentHashMap<>();

    public ChatManager(String localUsername, int listenPort) {
        this.localUsername = localUsername;
        this.chatServer = new ChatServer(localUsername, listenPort, this::handleIncomingSession);
        this.chatServer.start();
    }

    private void handleIncomingSession(ChatSession session) {
        sessions.put(session.remoteUsername(), session);
        // hook into UI / CLI here if you want
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
}
