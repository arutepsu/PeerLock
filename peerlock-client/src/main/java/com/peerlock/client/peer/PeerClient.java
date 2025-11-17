package com.peerlock.client.peer;

import com.peerlock.common.model.PeerInfo;

import java.util.List;

public interface PeerClient {

    /**
     * Tell the backend "I am online on host:port".
     */
    void sendHeartbeat(String accessToken, int port) throws Exception;

    /**
     * Get all currently online peers.
     */
    List<PeerInfo> getOnlinePeers(String accessToken) throws Exception;
}
