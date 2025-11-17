package com.peerlock.client.chat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.time.Instant;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TcpChatSession implements ChatSession {

    private final Socket socket;
    private final String localUsername;
    private final String remoteUsername;

    private final PrintWriter out;
    private final BufferedReader in;

    private final ExecutorService readerExecutor = Executors.newSingleThreadExecutor();
    private MessageListener listener;

    public TcpChatSession(Socket socket, String localUsername, String remoteUsername) {
        this.socket = socket;
        this.localUsername = localUsername;
        this.remoteUsername = remoteUsername;
        try {
            this.out = new PrintWriter(
                    new OutputStreamWriter(socket.getOutputStream()), true
            );
            this.in = new BufferedReader(
                    new InputStreamReader(socket.getInputStream())
            );
        } catch (IOException e) {
            throw new RuntimeException("Failed to create I/O streams for chat session", e);
        }

        startReaderLoop();
    }

    @Override
    public String remoteUsername() {
        return remoteUsername;
    }

    @Override
    public void sendMessage(String content) {
        long ts = System.currentTimeMillis();
        // Very simple protocol: timestamp|from|content
        String line = ts + "|" + localUsername + "|" + content.replace("\n", "\\n");
        out.println(line);
    }

    @Override
    public void onMessageReceived(MessageListener listener) {
        this.listener = listener;
    }

    private void startReaderLoop() {
        readerExecutor.submit(() -> {
            try {
                String line;
                while ((line = in.readLine()) != null) {
                    ChatMessage msg = parseLine(line);
                    if (listener != null) {
                        listener.onMessage(msg);
                    }
                }
            } catch (IOException e) {
                // connection closed or error
            } finally {
                closeQuietly();
            }
        });
    }

    private ChatMessage parseLine(String line) {
        String[] parts = line.split("\\|", 3);
        long ts = Long.parseLong(parts[0]);
        String from = parts[1];
        String content = parts[2].replace("\\n", "\n");
        return new ChatMessage(
                from,
                localUsername,
                Instant.ofEpochMilli(ts),
                content
        );
    }

    private void closeQuietly() {
        try {
            socket.close();
        } catch (IOException ignored) {
        }
        readerExecutor.shutdownNow();
    }

    @Override
    public void close() {
        closeQuietly();
    }
}
