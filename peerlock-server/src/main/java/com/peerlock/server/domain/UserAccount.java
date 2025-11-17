package com.peerlock.server.domain;

import java.time.Instant;

public record UserAccount(
        String username,
        String passwordHash,
        Instant createdAt
) {
    public UserAccount {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("username must not be blank");
        }
        if (passwordHash == null || passwordHash.isBlank()) {
            throw new IllegalArgumentException("passwordHash must not be blank");
        }
        if (createdAt == null) {
            throw new IllegalArgumentException("createdAt must not be null");
        }
    }
}
