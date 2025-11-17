package com.peerlock.server.service;

/**
 * Thrown when a requested peer cannot be found in the registry.
 */
public class PeerNotFoundException extends RuntimeException {

    public PeerNotFoundException(String username) {
        super("Peer not found: " + username);
    }
}
