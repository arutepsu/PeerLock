package com.peerlock.common.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record RegisterPeerRequest(
        String host,
        @Min(1) @Max(65535) int port
) {}
