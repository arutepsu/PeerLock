package com.peerlock.server.repository;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Very simple in-memory token store: accessToken -> username.
 *
 * For demo purposes. In a real system, you'd want
 * JWT or a persistent session store.
 */
public class SessionTokenStore {

    private final Map<String, String> tokens = new ConcurrentHashMap<>();

    public void store(String token, String username) {
        tokens.put(token, username);
    }

    public Optional<String> findUsernameByToken(String token) {
        return Optional.ofNullable(tokens.get(token));
    }

    public void revoke(String token) {
        tokens.remove(token);
    }
}
