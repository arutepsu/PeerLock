package com.peerlock.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point of the PeerLock application.
 *
 * For now this starts the signaling server.
 * Later, you could add profiles/modes for server vs client if you want.
 */
@SpringBootApplication
public class ServerAppLauncher {

    public static void main(String[] args) {
        SpringApplication.run(ServerAppLauncher.class, args);
    }
}