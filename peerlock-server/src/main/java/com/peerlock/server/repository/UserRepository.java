package com.peerlock.server.repository;

import com.peerlock.server.domain.UserAccount;

import java.util.Optional;

/**
 * Repository for user accounts (authentication).
 */
public interface UserRepository {

    void save(UserAccount user);

    Optional<UserAccount> findByUsername(String username);

    boolean existsByUsername(String username);
}
