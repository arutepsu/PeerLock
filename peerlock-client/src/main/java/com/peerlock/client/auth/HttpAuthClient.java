package com.peerlock.client.auth;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.peerlock.common.dto.AuthRequest;
import com.peerlock.common.dto.AuthResponse;

public class HttpAuthClient implements AuthClient {

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final String baseUrl; // e.g. "http://localhost:8080/api/v1/auth"

    public HttpAuthClient(String baseUrl) {
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = JsonConfig.MAPPER;
        this.baseUrl = baseUrl;
    }

    @Override
    public AuthResponse login(AuthRequest request) throws Exception {
        return postJson("/login", request, AuthResponse.class);
    }

    @Override
    public AuthResponse register(AuthRequest request) throws Exception {
        return postJson("/register", request, AuthResponse.class);
    }

    private <T> T postJson(String path, Object body, Class<T> responseType) throws Exception {
        String json = objectMapper.writeValueAsString(body);

        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + path))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());

        int status = response.statusCode();
        String responseBody = response.body();

        if (status >= 200 && status < 300) {
            return objectMapper.readValue(responseBody, responseType);
        }

        // Map your GlobalExceptionHandler responses to something meaningful
        // Handler currently returns raw String bodies, so just use that:
        throw new RuntimeException("Auth request failed: HTTP " + status + " - " + responseBody);
    }
}
