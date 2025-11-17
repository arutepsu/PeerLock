package com.peerlock;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point of the PeerLock application.
 *
 * For now this starts the signaling server.
 * Later, you could add profiles/modes for server vs client if you want.
 */
@SpringBootApplication
public class PeerLock {

    public static void main(String[] args) {
        SpringApplication.run(PeerLock.class, args);
    }
}
