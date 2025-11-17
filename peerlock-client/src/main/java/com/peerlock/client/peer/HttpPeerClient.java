package com.peerlock.client.peer;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.peerlock.common.model.PeerInfo;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

public class HttpPeerClient implements PeerClient {

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final String baseUrl; // e.g. "http://localhost:8080/api/v1/peers"

    public HttpPeerClient(String baseUrl) {
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper();
        this.baseUrl = baseUrl;
    }

    @Override
    public void sendHeartbeat(String accessToken, int port) throws Exception {
        HeartbeatRequest body = new HeartbeatRequest(port);
        String json = objectMapper.writeValueAsString(body);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/heartbeat"))
                .header("Authorization", "Bearer " + accessToken)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> response =
                httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new RuntimeException(
                    "Heartbeat failed: HTTP " + response.statusCode() + " - " + response.body());
        }
    }

    @Override
    public List<PeerInfo> getOnlinePeers(String accessToken) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/online"))
                .header("Authorization", "Bearer " + accessToken)
                .GET()
                .build();

        HttpResponse<String> response =
                httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() >= 200 && response.statusCode() < 300) {
            return objectMapper.readValue(
                    response.body(),
                    new TypeReference<List<PeerInfo>>() {}
            );
        }

        throw new RuntimeException(
                "getOnlinePeers failed: HTTP " + response.statusCode() + " - " + response.body());
    }

    // must match PeerController.HeartbeatRequest(int port)
    private record HeartbeatRequest(int port) {}
}
