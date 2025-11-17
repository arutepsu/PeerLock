package com.peerlock.server.service;

/**
 * Thrown when username or password is invalid on login.
 */
public class InvalidCredentialsException extends RuntimeException {

    public InvalidCredentialsException() {
        super("Invalid username or password");
    }
}
