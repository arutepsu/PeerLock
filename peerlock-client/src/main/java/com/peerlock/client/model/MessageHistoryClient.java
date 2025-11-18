package com.peerlock.client.model;

import java.time.Instant;
import java.util.List;

import com.peerlock.common.dto.ChatMessageDto;

public interface MessageHistoryClient {

    List<ChatMessageDto> getHistoryWith(String accessToken, String otherUsername, Instant since) throws Exception;

    void persistMessage(String accessToken, ChatMessageDto message) throws Exception;
}
