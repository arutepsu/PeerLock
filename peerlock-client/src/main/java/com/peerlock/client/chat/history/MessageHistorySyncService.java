package com.peerlock.client.chat.history;


import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import com.peerlock.client.model.MessageHistoryClient;
import com.peerlock.common.dto.ChatMessageDto;

public class MessageHistorySyncService {

    private final MessageStore localStore;
    private final MessageHistoryClient remoteClient;
    private final String accessToken;

    public MessageHistorySyncService(MessageStore localStore,
                                     MessageHistoryClient remoteClient,
                                     String accessToken) {
        this.localStore = localStore;
        this.remoteClient = remoteClient;
        this.accessToken = accessToken;
    }

    /**
     * Load local history + fetch missing remote messages + merge & return sorted.
     */
    public List<ChatMessageDto> loadAndSync(String otherUsername) {
        List<ChatMessageDto> local = localStore.getHistoryWith(otherUsername);

        Instant since = local.stream()
                .map(ChatMessageDto::timestamp)
                .max(Comparator.naturalOrder())
                .orElse(Instant.EPOCH);

        List<ChatMessageDto> remoteNew;
        try {
            remoteNew = remoteClient.getHistoryWith(accessToken, otherUsername, since);
        } catch (Exception e) {
            // In worst case we still show local history
            e.printStackTrace();
            remoteNew = List.of();
        }

        for (ChatMessageDto msg : remoteNew) {
            localStore.appendMessage(msg);
        }

        List<ChatMessageDto> merged = new ArrayList<>();
        merged.addAll(local);
        merged.addAll(remoteNew);
        merged.sort(Comparator.comparing(ChatMessageDto::timestamp));
        return merged;
    }

    /**
     * Push a newly sent/received message to remote storage.
     */
    public void pushToRemote(ChatMessageDto message) {
        try {
            remoteClient.persistMessage(accessToken, message);
        } catch (Exception e) {
            // For now: log only – offline mode still works with local history
            e.printStackTrace();
        }
    }
}
