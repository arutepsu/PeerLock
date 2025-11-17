package com.peerlock.ui.screen;

import java.util.function.Consumer;

import com.peerlock.ui.base.BaseScreen;
import com.peerlock.ui.event.AuthSuccessEvent;
import com.peerlock.ui.event.EventBus;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

public class LoginScreen extends BaseScreen {

    private final EventBus eventBus;

    private TextField usernameField;
    private PasswordField passwordField;
    private Button loginButton;
    private Label errorLabel;

    private Consumer<AuthSuccessEvent> authSuccessListener;

    public LoginScreen(EventBus eventBus /*, later: AuthClient authClient */) {
        this.eventBus = eventBus;
    }

    @Override
    protected void buildUI() {
        usernameField = new TextField();
        usernameField.setPromptText("Username");

        passwordField = new PasswordField();
        passwordField.setPromptText("Password");

        loginButton = new Button("Login");
        errorLabel  = new Label();

        VBox layout = new VBox(8, usernameField, passwordField, loginButton, errorLabel);
        root.setCenter(layout);
    }

    @Override
    protected void registerListeners() {
        loginButton.setOnAction(e -> {
            String user = usernameField.getText();
            // TODO: add validation + real backend call later

            // For now: immediately consider this "logged in"
            eventBus.publish(new AuthSuccessEvent(user));
        });

        // Optional debug listener. You can delete this if you want.
        authSuccessListener = event -> {
            errorLabel.setText("Welcome " + event.getUsername());
        };

        eventBus.subscribe(AuthSuccessEvent.class, authSuccessListener);
    }

    @Override
    protected void unregisterListeners() {
        if (authSuccessListener != null) {
            eventBus.unsubscribe(AuthSuccessEvent.class, authSuccessListener);
        }
    }
}
