package com.peerlock.ui.event;

public class LogoutEvent implements UiEvent {
    public final String currentUser;
    public final String accessToken;

    public LogoutEvent (String currentUser, String accessToken){
        this.currentUser = currentUser;
        this.accessToken = accessToken;
    }

    public String getCurrentUser() { return currentUser; }
    public String getAccessToken()  { return accessToken; }
}
