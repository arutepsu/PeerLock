package com.peerlock.ui.screen;

import com.peerlock.client.auth.AuthClient;
import com.peerlock.common.dto.AuthRequest;
import com.peerlock.common.dto.AuthResponse;
import com.peerlock.ui.base.BaseScreen;
import com.peerlock.ui.event.AuthSuccessEvent;
import com.peerlock.ui.event.EventBus;

import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

public class LoginScreen extends BaseScreen {

    private final EventBus eventBus;
    private final AuthClient authClient;

    private TextField usernameField;
    private PasswordField passwordField;
    private Button loginButton;
    private Label errorLabel;

    public LoginScreen(EventBus eventBus, AuthClient authClient) {
        this.eventBus = eventBus;
        this.authClient = authClient;
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
        loginButton.setOnAction(e -> doLogin());
        passwordField.setOnAction(e -> doLogin());
    }

    private void doLogin() {
        String user = usernameField.getText();
        String pass = passwordField.getText();

        if (user == null || user.isBlank() || pass == null || pass.isBlank()) {
            errorLabel.setText("Please enter username and password.");
            return;
        }

        errorLabel.setText("Logging in...");

        AuthRequest request = new AuthRequest(user, pass);

        new Thread(() -> {
            try {
                AuthResponse response = authClient.login(request);

                Platform.runLater(() -> {
                    eventBus.publish(new AuthSuccessEvent(
                            response.username(),
                            response.accessToken()
                    ));
                });
            } catch (Exception ex) {
                Platform.runLater(() ->
                        errorLabel.setText("Login failed: " + ex.getMessage())
                );
            }
        }, "auth-login-thread").start();
    }


    @Override
    protected void unregisterListeners() {
        // no EventBus subscriptions here anymore
    }
}
