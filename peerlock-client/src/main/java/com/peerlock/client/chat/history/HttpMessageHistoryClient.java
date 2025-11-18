package com.peerlock.client.chat.history;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.peerlock.client.model.MessageHistoryClient;
import com.peerlock.common.dto.ChatMessageDto;

public class HttpMessageHistoryClient implements MessageHistoryClient {

    private final HttpClient httpClient;
    private final ObjectMapper mapper;
    private final String baseUrl; // e.g. "http://localhost:8080/api/v1/messages"

    public HttpMessageHistoryClient(String baseUrl) {
        this.httpClient = HttpClient.newHttpClient();
        this.mapper = new ObjectMapper();
        this.baseUrl = baseUrl;
    }

    @Override
    public List<ChatMessageDto> getHistoryWith(String accessToken,
                                            String otherUsername,
                                            Instant since) throws Exception {
        String sinceParam = since != null ? since.toString() : "";
        String url = baseUrl + "/with/" + otherUsername;
        if (!sinceParam.isEmpty()) {
            url += "?since=" + sinceParam;
        }

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", "Bearer " + accessToken)
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() / 100 != 2) {
            throw new RuntimeException("Failed to fetch history: " + response.statusCode() + " " + response.body());
        }

        return mapper.readValue(response.body(), new TypeReference<List<ChatMessageDto>>() {});
    }

    @Override
    public void persistMessage(String accessToken, ChatMessageDto message) throws Exception {
        Map<String, Object> body = Map.of(
                "to", message.to(),
                "content", message.content(),
                "timestamp", message.timestamp().toString()
        );

        String json = mapper.writeValueAsString(body);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl))
                .header("Authorization", "Bearer " + accessToken)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() / 100 != 2) {
            throw new RuntimeException("Failed to persist message: " + response.statusCode() + " " + response.body());
        }
    }
}
