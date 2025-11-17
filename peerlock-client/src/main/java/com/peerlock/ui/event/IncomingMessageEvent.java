package com.peerlock.ui.event;

public class IncomingMessageEvent implements UiEvent {

    private final String fromUsername;
    private final String message;

    public IncomingMessageEvent(String fromUsername, String message) {
        this.fromUsername = fromUsername;
        this.message = message;
    }

    public String getFromUsername() {
        return fromUsername;
    }

    public String getMessage() {
        return message;
    }
}
