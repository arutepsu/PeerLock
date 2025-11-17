package com.peerlock.common.dto;
import java.time.Instant;

import com.peerlock.common.model.PeerStatus;

public record PeerInfoResponse(
        String username,
        String host,
        int port,
        PeerStatus status,
        Instant lastSeen
) {}
