package com.peerlock.client.auth;

import com.peerlock.common.dto.AuthRequest;
import com.peerlock.common.dto.AuthResponse;

public interface AuthClient {
    AuthResponse login(AuthRequest request) throws Exception;
}
// @TODO: Later you can add HttpAuthClient that calls your Spring AuthService REST endpoints. For now, this keeps the UI decoupled.