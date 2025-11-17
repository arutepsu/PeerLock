package com.peerlock.server.controller;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.peerlock.common.dto.PeerInfoResponse;
import com.peerlock.common.dto.RegisterPeerRequest;
import com.peerlock.common.model.PeerInfo;
import com.peerlock.server.service.PeerService;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/v1/peers")
public class PeerController {

    private final PeerService peerService;

    public PeerController(PeerService peerService) {
        this.peerService = peerService;
    }

    /**
     * Heartbeat / register endpoint:
     * - username comes from the authenticated principal
     * - host from the HTTP request (remote addr)
     * - port from the request body
     */
    @PostMapping("/heartbeat")
    public void heartbeat(Authentication authentication,
                          @RequestBody RegisterPeerRequest request,
                          HttpServletRequest servletRequest) {

        String username = (String) authentication.getPrincipal();
        String host = servletRequest.getRemoteAddr();

        peerService.heartbeat(username, host, request.port());
    }

    /**
     * Get list of online peers.
     */
    @GetMapping("/online")
    public List<PeerInfoResponse> getOnlinePeers(Authentication authentication) {
        String requestingUser = (String) authentication.getPrincipal();

        List<PeerInfo> peers = peerService.getOnlinePeers(requestingUser);

        return peers.stream()
                .map(this::toResponse)
                .toList();
    }

    /**
     * Get info about a specific peer by username.
     */
    @GetMapping("/{username}")
    public PeerInfoResponse getPeer(@PathVariable String username,
                                    Authentication authentication) {
        String requestingUser = (String) authentication.getPrincipal();

        PeerInfo peer = peerService.getPeer(requestingUser, username);
        return toResponse(peer);
    }

    
    @GetMapping("/me")
    public PeerInfoResponse getMyPeer(Authentication authentication) {
        String username = (String) authentication.getPrincipal();

        PeerInfo peer = peerService.getOwnPeer(username);
        return toResponse(peer);
    }

    private PeerInfoResponse toResponse(PeerInfo peer) {
        return new PeerInfoResponse(
                peer.username(),
                peer.host(),
                peer.port(),
                peer.status(),
                peer.lastSeen()
        );
    }
}
