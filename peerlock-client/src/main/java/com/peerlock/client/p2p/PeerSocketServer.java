package com.peerlock.client.p2p;

import com.peerlock.common.dto.ChatMessageDto;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.peerlock.client.chat.history.MessageHistorySyncService;
import com.peerlock.client.chat.history.MessageStore;
import com.peerlock.ui.event.EventBus;
import com.peerlock.ui.event.IncomingMessageEvent;

public class PeerSocketServer {

    private final int port;
    private final EventBus eventBus;
    private final String localUsername;
    private final MessageStore messageStore;
    private final MessageHistorySyncService historySyncService;
    private final ExecutorService ioExecutor = Executors.newCachedThreadPool();

    private volatile boolean running = false;
    private Thread serverThread;
    private ServerSocket serverSocket;

    public PeerSocketServer(int port,
                            EventBus eventBus,
                            String localUsername,
                            MessageStore messageStore,
                            MessageHistorySyncService historySyncService) {
        this.port = port;
        this.eventBus = eventBus;
        this.localUsername = localUsername;
        this.messageStore = messageStore;
        this.historySyncService = historySyncService;
    }

    public PeerSocketServer(EventBus eventBus, MessageHistorySyncService historySyncService, String localUsername, MessageStore messageStore, int port) {
        this.eventBus = eventBus;
        this.historySyncService = historySyncService;
        this.localUsername = localUsername;
        this.messageStore = messageStore;
        this.port = port;
    }

    public void start() {
        if (running) {
            return;
        }
        running = true;

        serverThread = new Thread(this::runServer, "peer-socket-server-" + port);
        serverThread.setDaemon(true);
        serverThread.start();
    }

    public void stop() {
        running = false;
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (Exception ignored) {
        }
        if (serverThread != null) {
            serverThread.interrupt();
        }
        ioExecutor.shutdownNow();
    }

    private void runServer() {
        try (ServerSocket ss = new ServerSocket(port)) {
            this.serverSocket = ss;

            while (running) {
                Socket socket = ss.accept();
                handleClient(socket);
            }
        } catch (Exception e) {
            if (running) {
                e.printStackTrace(); // TODO: proper logging
            }
        }
    }

    private void handleClient(Socket socket) {
        Thread clientThread = new Thread(() -> {
            try (socket;
                 BufferedReader reader = new BufferedReader(
                         new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8))) {

                // first line: who is sending
                String fromUser = reader.readLine();
                if (fromUser == null) {
                    return;
                }

                String line;
                while ((line = reader.readLine()) != null) {
                    // 1) Create ChatMessageDto
                    ChatMessageDto msg = new ChatMessageDto(
                            fromUser,
                            localUsername,
                            line,
                            Instant.now()
                    );

                    // 2) Persist locally
                    messageStore.appendMessage(msg);

                    // 3) Sync to server (async)
                    ioExecutor.submit(() -> {
                        try {
                            historySyncService.pushToRemote(msg);
                        } catch (Exception e) {
                            e.printStackTrace(); // TODO: proper logging
                        }
                    });

                    // 4) Notify UI via EventBus (keep existing behavior)
                    eventBus.publish(new IncomingMessageEvent(fromUser, line));
                }
            } catch (Exception e) {
                // TODO: logging
            }
        }, "peer-client-handler-" + socket.getRemoteSocketAddress());

        clientThread.setDaemon(true);
        clientThread.start();
    }
}
