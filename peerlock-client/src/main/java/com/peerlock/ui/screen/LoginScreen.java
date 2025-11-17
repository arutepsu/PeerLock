package com.peerlock.ui.screen;

import com.peerlock.ui.base.BaseScreen;
import com.peerlock.ui.event.AuthSuccessEvent;
import com.peerlock.ui.event.EventBus;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

import java.util.function.Consumer;

public class LoginScreen extends BaseScreen {

    private final EventBus eventBus;

    private TextField usernameField;
    private TextField passwordField;
    private Button loginButton;
    private Label errorLabel;

    private Consumer<AuthSuccessEvent> authSuccessListener;

    public LoginScreen(EventBus eventBus /*, AuthService authService, ... */) {
        this.eventBus = eventBus;
    }

    @Override
    protected void buildUI() {
        usernameField = new TextField();
        passwordField = new TextField();
        loginButton   = new Button("Login");
        errorLabel    = new Label();

        VBox layout = new VBox(8, usernameField, passwordField, loginButton, errorLabel);
        root.setCenter(layout);
    }

    @Override
    protected void registerListeners() {
        // Local UI event
        loginButton.setOnAction(e -> {
            String user = usernameField.getText();
            // TODO: call AuthService, then on success:
            eventBus.publish(new AuthSuccessEvent(user));
        });

        // App-wide event (for demonstration; usually another screen listens)
        authSuccessListener = event -> {
            // could update UI / show message, but in reality MainScreen would react
            errorLabel.setText("Welcome " + event.getUsername());
        };

        eventBus.subscribe(AuthSuccessEvent.class, authSuccessListener);
    }

    @Override
    protected void unregisterListeners() {
        // remove from event bus to avoid memory leaks
        if (authSuccessListener != null) {
            eventBus.unsubscribe(AuthSuccessEvent.class, authSuccessListener);
        }
    }
}
