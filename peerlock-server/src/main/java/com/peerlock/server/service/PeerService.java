package com.peerlock.server.service;

import com.peerlock.common.model.PeerStatus;
import com.peerlock.server.domain.PeerInfo;
import com.peerlock.server.repository.PeerRegistry;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

/**
 * Application service that encapsulates peer-related business logic.
 */
@Service
public class PeerService {

    private final PeerRegistry registry;

    /**
     * How long a peer is considered "online" since its lastSeen timestamp.
     * You can later move this to configuration (application.yml).
     */
    private final Duration onlineTimeout = Duration.ofMinutes(2);

    public PeerService(PeerRegistry registry) {
        this.registry = registry;
    }

    /**
     * Register a new peer or update an existing one for the given user.
     *
     * @param username the owning user's username (from auth token)
     * @param host     host/ip where the P2P client is listening
     * @param port     TCP port where the P2P client is listening
     * @return the stored PeerInfo
     */
    public PeerInfo registerOrUpdate(String username, String host, int port) {
        PeerInfo info = new PeerInfo(
                username,
                host,
                port,
                PeerStatus.ONLINE,
                Instant.now()
        );
        registry.registerOrUpdate(info);
        return info;
    }

    /**
     * List peers that are currently considered online.
     */
    public List<PeerInfo> listOnlinePeers() {
        Instant threshold = Instant.now().minus(onlineTimeout);
        return registry.listAll().stream()
                .filter(p -> p.status() == PeerStatus.OFFLINE || p.status() == PeerStatus.ONLINE) // keep if you later support OFFLINE explicitly
                .filter(p -> p.status() == PeerStatus.ONLINE)
                .filter(p -> p.lastSeen().isAfter(threshold))
                .toList();
    }

    /**
     * Get a peer by username or throw if not found.
     */
    public PeerInfo getPeer(String username) {
        return registry.findByUsername(username)
                .orElseThrow(() -> new PeerNotFoundException(username));
    }
}
