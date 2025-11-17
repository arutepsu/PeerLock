package com.peerlock.ui.event;

public class AuthSuccessEvent implements UiEvent {
    private final String username;

    public AuthSuccessEvent(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }
}
