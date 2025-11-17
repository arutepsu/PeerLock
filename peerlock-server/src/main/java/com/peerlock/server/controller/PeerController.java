package com.peerlock.server.controller;

import java.util.List;

import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

    @PostMapping("/heartbeat")
    public void heartbeat(@RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader,
                          @RequestBody HeartbeatRequest request,
                          HttpServletRequest servletRequest) {
        String token = extractToken(authHeader);
        String host = servletRequest.getRemoteAddr();

        peerService.heartbeat(token, host, request.port());
    }

    @GetMapping("/online")
    public List<PeerInfo> getOnlinePeers(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader) {
        String token = extractToken(authHeader);
        return peerService.getOnlinePeers(token);
    }

    @GetMapping("/{username}")
    public PeerInfo getPeer(
            @PathVariable String username,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader) {
        String token = extractToken(authHeader);
        return peerService.getPeer(token, username);
    }

    public record HeartbeatRequest(int port) {}

    private String extractToken(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new IllegalArgumentException("Missing or invalid Authorization header");
        }
        return authHeader.substring("Bearer ".length());
    }
}
