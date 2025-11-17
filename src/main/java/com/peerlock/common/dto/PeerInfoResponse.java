package com.peerlock.common.dto;

import com.peerlock.common.model.PeerStatus;

import java.time.Instant;

public record PeerInfoResponse(
        String username,
        String host,
        int port,
        PeerStatus status,
        Instant lastSeen
) {}
