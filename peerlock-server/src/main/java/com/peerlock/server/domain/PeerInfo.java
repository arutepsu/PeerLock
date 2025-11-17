package com.peerlock.server.domain;

import com.peerlock.common.model.PeerStatus;

import java.time.Instant;

public record PeerInfo(
        String username,
        String host,
        int port,
        PeerStatus status,
        Instant lastSeen
) {
    public PeerInfo {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("username must not be blank");
        }
        if (host == null || host.isBlank()) {
            throw new IllegalArgumentException("host must not be blank");
        }
        if (port <= 0 || port > 65535) {
            throw new IllegalArgumentException("port must be between 1 and 65535");
        }
        if (status == null) {
            throw new IllegalArgumentException("status must not be null");
        }
        if (lastSeen == null) {
            throw new IllegalArgumentException("lastSeen must not be null");
        }
    }
}
