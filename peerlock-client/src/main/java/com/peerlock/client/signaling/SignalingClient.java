package com.peerlock.client.signaling;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.peerlock.client.model.AuthSession;
import com.peerlock.common.dto.AuthRequest;
import com.peerlock.common.dto.AuthResponse;
import com.peerlock.common.dto.PeerInfoResponse;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * HTTP client talking to the PeerLock signaling server.
 */
public class SignalingClient {

    private final String serverBaseUrl; // e.g. "http://localhost:8080"
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public SignalingClient(String serverBaseUrl) {
        this.serverBaseUrl = serverBaseUrl.endsWith("/")
                ? serverBaseUrl.substring(0, serverBaseUrl.length() - 1)
                : serverBaseUrl;
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper();
    }

    public AuthSession register(String username, String password) throws IOException, InterruptedException {
        AuthRequest requestBody = new AuthRequest(username, password);
        String json = objectMapper.writeValueAsString(requestBody);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(serverBaseUrl + "/api/v1/auth/register"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json, StandardCharsets.UTF_8))
                .build();

        HttpResponse<String> response =
                httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        if (response.statusCode() >= 400) {
            throw new RuntimeException("Register failed: HTTP " + response.statusCode() + " - " + response.body());
        }

        AuthResponse authResponse = objectMapper.readValue(response.body(), AuthResponse.class);
        return new AuthSession(authResponse.username(), authResponse.accessToken(), serverBaseUrl);
    }

    public AuthSession login(String username, String password) throws IOException, InterruptedException {
        AuthRequest requestBody = new AuthRequest(username, password);
        String json = objectMapper.writeValueAsString(requestBody);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(serverBaseUrl + "/api/v1/auth/login"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json, StandardCharsets.UTF_8))
                .build();

        HttpResponse<String> response =
                httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        if (response.statusCode() >= 400) {
            throw new RuntimeException("Login failed: HTTP " + response.statusCode() + " - " + response.body());
        }

        AuthResponse authResponse = objectMapper.readValue(response.body(), AuthResponse.class);
        return new AuthSession(authResponse.username(), authResponse.accessToken(), serverBaseUrl);
    }

    public List<PeerInfoResponse> listOnlinePeers(AuthSession session) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(serverBaseUrl + "/api/v1/peers/online"))
                .header("Authorization", "Bearer " + session.accessToken())
                .GET()
                .build();

        HttpResponse<String> response =
                httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        if (response.statusCode() >= 400) {
            throw new RuntimeException("listOnlinePeers failed: HTTP " + response.statusCode() + " - " + response.body());
        }

        return objectMapper.readValue(
                response.body(),
                new TypeReference<List<PeerInfoResponse>>() {}
        );
    }

    public PeerInfoResponse getPeer(AuthSession session, String username) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(serverBaseUrl + "/api/v1/peers/" + username))
                .header("Authorization", "Bearer " + session.accessToken())
                .GET()
                .build();

        HttpResponse<String> response =
                httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        if (response.statusCode() >= 400) {
            throw new RuntimeException("getPeer failed: HTTP " + response.statusCode() + " - " + response.body());
        }

        return objectMapper.readValue(response.body(), PeerInfoResponse.class);
    }
}
