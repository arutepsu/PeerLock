package com.peerlock.client.auth;

import com.peerlock.common.dto.AuthRequest;
import com.peerlock.common.dto.AuthResponse;

public interface AuthClient {

    AuthResponse login(AuthRequest request) throws Exception;

    AuthResponse register(AuthRequest request) throws Exception;
}
