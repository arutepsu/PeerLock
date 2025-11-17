package com.peerlock.ui;

import com.peerlock.ui.base.BaseScreen;
import com.peerlock.ui.event.AuthSuccessEvent;
import com.peerlock.ui.event.EventBus;
import com.peerlock.ui.screen.LoginScreen;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

/**
 * Entry point of the PeerLock JavaFX client.
 *
 * Responsibilities:
 *  - Create shared infrastructure (EventBus, ScreenManager).
 *  - Show the initial screen (LoginScreen).
 *  - React to global events (e.g. AuthSuccessEvent) to switch screens.
 */
public class RootApp extends Application {

    private EventBus eventBus;
    private ScreenManager screenManager;

    @Override
    public void init() {
        this.eventBus = new EventBus();
    }

    @Override
    public void start(Stage primaryStage) {
        Scene scene = new Scene(new BorderPane(), 1024, 640);

        this.screenManager = new ScreenManager(scene);

        registerGlobalEventHandlers();

        BaseScreen loginScreen = new LoginScreen(eventBus /*, authService, ... */);
        screenManager.show(loginScreen);

        primaryStage.setTitle("PeerLock");
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(800);
        primaryStage.setMinHeight(500);
        primaryStage.show();
    }

    private void registerGlobalEventHandlers() {
        eventBus.subscribe(AuthSuccessEvent.class, this::onAuthSuccess);
    }

    private void onAuthSuccess(AuthSuccessEvent event) {
        System.out.println("Authenticated as: " + event.getUsername());

        // BaseScreen mainScreen = new MainScreen(eventBus /*, other deps */);
        // screenManager.show(mainScreen);
    }

    @Override
    public void stop() {
        BaseScreen current = screenManager.getCurrentScreen();
        if (current != null) {
            current.destroy();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
