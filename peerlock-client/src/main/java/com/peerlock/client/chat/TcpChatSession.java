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
import com.peerlock.client.crypto.Crypto;

public class TcpChatSession implements ChatSession {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final Socket socket;
    private final String localUsername;
    private final String remoteUsername;
    private final SecretKey sessionKey;
    private final PrintWriter out;
    private final BufferedReader in;

    private final ExecutorService readerExecutor = Executors.newSingleThreadExecutor();
    private MessageListener listener;

    public TcpChatSession(Socket socket,
                          String localUsername,
                          String remoteUsername,
                          SecretKey sessionKey) {
        this.socket = socket;
        this.localUsername = localUsername;
        this.remoteUsername = remoteUsername;
        this.sessionKey = sessionKey;
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
    public void sendMessage(ChatMessage message) {
        try {
            // Optional: sanity check sender/to
            // if (!message.from().equals(localUsername) || !message.to().equals(remoteUsername)) { ... }

            byte[] plaintext = MAPPER.writeValueAsBytes(message);
            Crypto.EncryptedPayload enc = Crypto.encrypt(sessionKey, plaintext);

            String line = "MSG|" + enc.nonceBase64() + "|" + enc.ciphertextBase64();
            out.println(line);
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
                        ChatMessage msg = parseEncryptedMessage(line);
                        if (listener != null && msg != null) {
                            listener.onMessage(msg);
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

    private ChatMessage parseEncryptedMessage(String line) {
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

            return MAPPER.readValue(json, ChatMessage.class);
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
