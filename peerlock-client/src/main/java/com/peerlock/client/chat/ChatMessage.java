package com.peerlock.client.chat;

import java.time.Instant;

public record ChatMessage(
        String from,
        String to,
        Instant timestamp,
        String content
) {
}
