package com.peerlock.ui;

import com.peerlock.client.auth.AuthClient;
import com.peerlock.client.auth.HttpAuthClient;
import com.peerlock.ui.base.BaseScreen;
import com.peerlock.ui.event.AuthSuccessEvent;
import com.peerlock.ui.event.EventBus;
import com.peerlock.ui.screen.LoginScreen;
import com.peerlock.ui.screen.MainScreen;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class RootApp extends Application {

    private EventBus eventBus;
    private ScreenManager screenManager;
    private AuthClient authClient;

    @Override
    public void init() {
        this.eventBus = new EventBus();
        this.authClient = new HttpAuthClient("http://localhost:8080/api/v1/auth");
    }

    @Override
    public void start(Stage primaryStage) {
        Scene scene = new Scene(new BorderPane(), 1024, 640);
        this.screenManager = new ScreenManager(scene);

        registerGlobalEventHandlers();

        BaseScreen loginScreen = new LoginScreen(eventBus, authClient);
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

        BaseScreen mainScreen = new MainScreen(
                eventBus,
                event.getUsername(),
                event.getAccessToken()
        );
        screenManager.show(mainScreen);
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
