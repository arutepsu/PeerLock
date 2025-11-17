package com.peerlock.server.service;

/**
 * Thrown when an access token is missing, invalid, or unknown.
 */
public class InvalidTokenException extends RuntimeException {

    public InvalidTokenException() {
        super("Invalid or expired access token");
    }
}
