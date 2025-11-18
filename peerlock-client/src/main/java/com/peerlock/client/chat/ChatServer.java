package com.peerlock.client.chat;


import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.crypto.SecretKey;

import com.peerlock.client.chat.history.MessageHistorySyncService;
import com.peerlock.client.chat.history.MessageStore;

public class ChatServer implements AutoCloseable {

    private final String localUsername;
    private final int port;
    private final MessageStore messageStore;
    private final MessageHistorySyncService historySyncService;
    private final IncomingSessionHandler handler;

    private final ExecutorService acceptorExecutor = Executors.newSingleThreadExecutor();
    private volatile boolean running = false;
    private ServerSocket serverSocket;

    public ChatServer(String localUsername,
                      int port,
                      MessageStore messageStore,
                      MessageHistorySyncService historySyncService,
                      IncomingSessionHandler handler) {
        this.localUsername = localUsername;
        this.port = port;
        this.messageStore = messageStore;
        this.historySyncService = historySyncService;
        this.handler = handler;
    }

    public void start() {
        if (running) return;
        running = true;
        acceptorExecutor.submit(this::acceptLoop);
    }

    private void acceptLoop() {
        try (ServerSocket server = new ServerSocket(port)) {
            this.serverSocket = server;
            while (running) {
                Socket socket = server.accept();

                ChatHandshake.Result result =
                        ChatHandshake.performServerHandshake(socket, localUsername, "peerlock-client-0.1");

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

                handler.onIncomingSession(session);
            }
        } catch (IOException e) {
            if (running) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void close() throws Exception {
        running = false;
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException ignored) {}
        acceptorExecutor.shutdownNow();
    }

    @FunctionalInterface
    public interface IncomingSessionHandler {
        void onIncomingSession(ChatSession session);
    }
}
