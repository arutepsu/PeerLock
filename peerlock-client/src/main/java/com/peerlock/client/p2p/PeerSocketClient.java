package com.peerlock.client.p2p;

import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.io.IOException;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.peerlock.client.chat.history.MessageHistorySyncService;
import com.peerlock.client.chat.history.MessageStore;
import com.peerlock.common.dto.ChatMessageDto;
import com.peerlock.common.model.PeerInfo;

public class PeerSocketClient {

    private final String currentUsername;
    private final MessageStore messageStore;
    private final MessageHistorySyncService historySyncService;
    private final ExecutorService ioExecutor = Executors.newCachedThreadPool();

    public PeerSocketClient(String currentUsername,
                            MessageStore messageStore,
                            MessageHistorySyncService historySyncService) {
        this.currentUsername = currentUsername;
        this.messageStore = messageStore;
        this.historySyncService = historySyncService;
    }

    /**
     * Convenience overload for UI code: just pass peer + text.
     */
    public void sendMessage(PeerInfo target, String text) throws Exception {
        ChatMessageDto msg = new ChatMessageDto(
                currentUsername,
                target.username(),
                text,
                Instant.now()
        );
        sendMessage(target.host(), target.port(), msg);
    }

    /**
     * Low-level method: send a fully built ChatMessageDto.
     */
    public void sendMessage(String host, int port, ChatMessageDto msg) throws Exception {
        // 1) Send over TCP (simple protocol: username line + message line)
        try (Socket socket = new Socket(host, port);
             BufferedWriter writer = new BufferedWriter(
                     new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8))) {

            // first line: who am I
            writer.write(currentUsername);
            writer.newLine();

            // second line: just the content (remote side knows "from" from first line)
            writer.write(msg.content());
            writer.newLine();
            writer.flush();
        }

        // 2) Store locally
        messageStore.appendMessage(msg);

        // 3) Push to server (async, don't block UI)
        ioExecutor.submit(() -> {
            try {
                historySyncService.pushToRemote(msg);
            } catch (Exception e) {
                // for now: log only; still have local history
                e.printStackTrace();
            }
        });
    }
}
