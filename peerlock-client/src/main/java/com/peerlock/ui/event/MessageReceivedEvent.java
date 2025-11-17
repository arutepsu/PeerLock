package com.peerlock.ui.event;

public class MessageReceivedEvent implements UiEvent {
    private final String fromPeer;
    private final String message;

    public MessageReceivedEvent(String fromPeer, String message) {
        this.fromPeer = fromPeer;
        this.message = message;
    }

    public String getFromPeer() { return fromPeer; }
    public String getMessage()  { return message; }
}