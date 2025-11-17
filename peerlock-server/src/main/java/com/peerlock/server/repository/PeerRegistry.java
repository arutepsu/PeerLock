package com.peerlock.server.repository;

import java.util.List;
import java.util.Optional;

import com.peerlock.common.model.PeerInfo;

/**
 * Abstraction over peer storage.
 *
 * v1: in-memory implementation.
 * Later: database, Redis, etc.
 */
public interface PeerRegistry {

    /**
     * Register a new peer or update an existing one.
     */
    void registerOrUpdate(PeerInfo peerInfo);

    /**
     * Find a peer by its username.
     */
    Optional<PeerInfo> findByUsername(String username);

    /**
     * List all stored peers (online/offline).
     * Online filtering is done in the service layer.
     */
    List<PeerInfo> listAll();
}
