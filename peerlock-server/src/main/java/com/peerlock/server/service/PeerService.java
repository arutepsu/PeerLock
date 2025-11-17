package com.peerlock.server.service;

import java.time.Instant;
import java.util.List;

import org.springframework.stereotype.Service;

import com.peerlock.common.model.PeerStatus;
import com.peerlock.server.domain.PeerInfo;
import com.peerlock.server.repository.PeerRegistry;

@Service
public class PeerService {

    private final PeerRegistry peerRegistry;
    private final AuthService authService;

    public PeerService(PeerRegistry peerRegistry, AuthService authService) {
        this.peerRegistry = peerRegistry;
        this.authService = authService;
    }

    public void heartbeat(String accessToken, String host, int port) {
        String username = authService.getUsernameFromToken(accessToken);

        PeerInfo info = new PeerInfo(
                username,
                host,
                port,
                PeerStatus.ONLINE,
                Instant.now()
        );
        peerRegistry.registerOrUpdate(info);
    }

    public List<PeerInfo> getOnlinePeers(String accessToken) {
        authService.getUsernameFromToken(accessToken);

        return peerRegistry.listAll().stream()
                .filter(p -> p.status() == PeerStatus.ONLINE)
                .toList();
    }

    public PeerInfo getPeer(String accessToken, String targetUsername) {
        authService.getUsernameFromToken(accessToken);

        return peerRegistry.findByUsername(targetUsername)
                .orElseThrow(() -> new PeerNotFoundException("Peer not found: " + targetUsername));
    }

    public void markOffline(String accessToken) {
        String username = authService.getUsernameFromToken(accessToken);
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
}