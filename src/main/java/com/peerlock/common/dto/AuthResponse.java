package com.peerlock.common.dto;

/**
 * Response returned by /auth/register and /auth/login.
 */
public record AuthResponse(
        String username,
        String accessToken
) {
}
