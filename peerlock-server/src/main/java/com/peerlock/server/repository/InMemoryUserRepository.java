package com.peerlock.server.repository;

import com.peerlock.server.domain.UserAccount;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory implementation of UserRepository.
 */
public class InMemoryUserRepository implements UserRepository {

    private final Map<String, UserAccount> users = new ConcurrentHashMap<>();

    @Override
    public void save(UserAccount user) {
        users.put(user.username(), user);
    }

    @Override
    public Optional<UserAccount> findByUsername(String username) {
        return Optional.ofNullable(users.get(username));
    }

    @Override
    public boolean existsByUsername(String username) {
        return users.containsKey(username);
    }
}

