package com.peerlock.server.messages;

import java.time.Instant;

public record SendMessageRequest(
        String to,
        String content,
        Instant timestamp
) { }
