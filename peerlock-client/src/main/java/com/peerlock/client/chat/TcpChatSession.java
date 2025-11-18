package com.peerlock.client.chat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.crypto.SecretKey;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.peerlock.client.chat.history.MessageHistorySyncService;
import com.peerlock.client.chat.history.MessageStore;
import com.peerlock.client.crypto.Crypto;
import com.peerlock.common.dto.ChatMessageDto;

public class TcpChatSession implements ChatSession {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final Socket socket;
    private final String localUsername;
    private final String remoteUsername;
    private final SecretKey sessionKey;
    private final PrintWriter out;
    private final BufferedReader in;
    private final MessageStore messageStore;
    private final MessageHistorySyncService historySyncService;
    private final ExecutorService readerExecutor = Executors.newSingleThreadExecutor();

    private MessageListener listener;

    public TcpChatSession(Socket socket,
                          String localUsername,
                          String remoteUsername,
                          SecretKey sessionKey,
                          MessageStore messageStore,
                          MessageHistorySyncService historySyncService) {
        this.socket = socket;
        this.localUsername = localUsername;
        this.remoteUsername = remoteUsername;
        this.sessionKey = sessionKey;
        this.messageStore = messageStore;
        this.historySyncService = historySyncService;

        try {
            this.out = new PrintWriter(
                    new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true
            );
            this.in = new BufferedReader(
                    new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8)
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
    public void sendMessage(ChatMessageDto message) {
        try {
            // Optional: sanity checks
            // if (!message.from().equals(localUsername) || !message.to().equals(remoteUsername)) { ... }

            byte[] plaintext = MAPPER.writeValueAsBytes(message);
            Crypto.EncryptedPayload enc = Crypto.encrypt(sessionKey, plaintext);

            String line = "MSG|" + enc.nonceBase64() + "|" + enc.ciphertextBase64();
            out.println(line);

            // 🔹 Persist locally
            messageStore.appendMessage(message);

            // 🔹 Push to remote (non-blocking)
            readerExecutor.submit(() -> historySyncService.pushToRemote(message));
        } catch (Exception e) {
            // log in real app
            e.printStackTrace();
        }
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
                    if (line.startsWith("MSG|")) {
                        ChatMessageDto msg = parseEncryptedMessage(line);
                        if (msg != null) {
                            // 🔹 Persist incoming message
                            messageStore.appendMessage(msg);
                            // 🔹 Push to remote
                            readerExecutor.submit(() -> historySyncService.pushToRemote(msg));

                            if (listener != null) {
                                listener.onMessage(msg);
                            }
                        }
                    } else if (line.startsWith("BYE|")) {
                        // optional: handle graceful close
                        break;
                    } else {
                        // unknown / legacy line, ignore or log
                        // System.out.println("Unknown message: " + line);
                    }
                }
            } catch (IOException e) {
                // connection closed or error
            } finally {
                closeQuietly();
            }
        });
    }

    private ChatMessageDto parseEncryptedMessage(String line) {
        try {
            // MSG|nonce|ciphertext
            String[] parts = line.split("\\|", 3);
            if (parts.length < 3) {
                // invalid message
                return null;
            }
            String nonceB64 = parts[1];
            String cipherB64 = parts[2];

            byte[] plaintext = Crypto.decrypt(sessionKey, nonceB64, cipherB64);
            String json = new String(plaintext, StandardCharsets.UTF_8);

            return MAPPER.readValue(json, ChatMessageDto.class);
        } catch (Exception e) {
            // log in real app
            e.printStackTrace();
            return null;
        }
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
