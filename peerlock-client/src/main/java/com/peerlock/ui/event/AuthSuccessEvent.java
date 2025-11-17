package com.peerlock.ui.event;

public class AuthSuccessEvent implements UiEvent {

    private final String username;
    private final String accessToken;

    public AuthSuccessEvent(String username, String accessToken) {
        this.username = username;
        this.accessToken = accessToken;
    }

    public String getUsername() {
        return username;
    }

    public String getAccessToken() {
        return accessToken;
    }
}
