package com.peerlock.server.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.springframework.stereotype.Component;

import com.peerlock.server.domain.PeerInfo;

/**
 * Simple thread-safe in-memory PeerRegistry implementation.
 */
@Component
public class InMemoryPeerRegistry implements PeerRegistry {

    // username -> PeerInfo
    private final ConcurrentMap<String, PeerInfo> peers = new ConcurrentHashMap<>();

    @Override
    public void registerOrUpdate(PeerInfo peerInfo) {
        peers.put(peerInfo.username(), peerInfo);
    }

    @Override
    public Optional<PeerInfo> findByUsername(String username) {
        return Optional.ofNullable(peers.get(username));
    }

    @Override
    public List<PeerInfo> listAll() {
        return new ArrayList<>(peers.values());
    }

    @Override
    public String toString() {
        return "InMemoryPeerRegistry{" +
                "peers=" + peers +
                '}';
    }
}
