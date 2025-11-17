package com.peerlock.server.service;

/**
 * Thrown when trying to register with a username that already exists.
 */
public class UsernameAlreadyExistsException extends RuntimeException {

    public UsernameAlreadyExistsException(String username) {
        super("Username already exists: " + username);
    }
}
