package com.peerlock.client.model;

/**
 * Represents an authenticated session against the PeerLock signaling server.
 */
public record AuthSession(
        String username,
        String accessToken,
        String serverBaseUrl // e.g. "http://localhost:8080"
) {
}
