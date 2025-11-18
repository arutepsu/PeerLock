package com.peerlock.common.dto;

import java.time.Instant;

public record ChatMessageDto(
        String from,
        String to,
        String content,
        Instant timestamp
) { }