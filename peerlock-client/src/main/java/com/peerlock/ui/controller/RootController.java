package com.peerlock.ui.controller;

import com.peerlock.ui.ScreenManager;
import com.peerlock.ui.event.AuthSuccessEvent;
import com.peerlock.ui.event.EventBus;


public class RootController {

    private final ScreenManager screenManager;
    private final EventBus eventBus;

    public RootController(ScreenManager screenManager, EventBus eventBus) {
        this.screenManager = screenManager;
        this.eventBus = eventBus;

        eventBus.subscribe(AuthSuccessEvent.class, this::onAuthSuccess);
    }

    private void onAuthSuccess(AuthSuccessEvent e) {
        // switch to MainScreen
        // screenManager.show(new MainScreen(eventBus /*, other deps */));
    }
}
