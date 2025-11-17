package com.peerlock.server.service;

import java.time.Instant;
import java.util.List;

import org.springframework.stereotype.Service;

import com.peerlock.common.model.PeerInfo;
import com.peerlock.common.model.PeerStatus;
import com.peerlock.server.repository.PeerRegistry;

@Service
public class PeerService {

    private final PeerRegistry peerRegistry;

    public PeerService(PeerRegistry peerRegistry) {
        this.peerRegistry = peerRegistry;
    }

    /**
     * Register or update the current user's peer info.
     */
    public void heartbeat(String username, String host, int port) {
        PeerInfo info = new PeerInfo(
                username,
                host,
                port,
                PeerStatus.ONLINE,
                Instant.now()
        );
        peerRegistry.registerOrUpdate(info);
    }

    /**
     * List all online peers. You can choose to exclude the requesting user if you want.
     */
    public List<PeerInfo> getOnlinePeers(String requestingUsername) {
        return peerRegistry.listAll().stream()
                .filter(p -> p.status() == PeerStatus.ONLINE)
                // .filter(p -> !p.username().equals(requestingUsername)) // optional: hide self
                .toList();
    }

    /**
     * Get a specific peer by username.
     */
    public PeerInfo getPeer(String requestingUsername, String targetUsername) {
        return peerRegistry.findByUsername(targetUsername)
                .orElseThrow(() -> new PeerNotFoundException("Peer not found: " + targetUsername));
    }

    /**
     * Mark the current user's peer as OFFLINE.
     */
    public void markOffline(String username) {
        peerRegistry.findByUsername(username).ifPresent(current -> {
            PeerInfo updated = new PeerInfo(
                    current.username(),
                    current.host(),
                    current.port(),
                    PeerStatus.OFFLINE,
                    Instant.now()
            );
            peerRegistry.registerOrUpdate(updated);
        });
    }

    public PeerInfo getOwnPeer(String username) {
        return peerRegistry.findByUsername(username)
                .orElseThrow(() -> new PeerNotFoundException("Peer not registered: " + username));
    }
}
