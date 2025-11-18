package com.peerlock.ui.screen;

import com.peerlock.client.auth.AuthClient;
import com.peerlock.common.dto.AuthRequest;
import com.peerlock.common.dto.AuthResponse;
import com.peerlock.ui.base.BaseScreen;
import com.peerlock.ui.event.AuthSuccessEvent;
import com.peerlock.ui.event.EventBus;
import com.peerlock.ui.utils.Styles;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class LoginScreen extends BaseScreen {

    private final EventBus eventBus;
    private final AuthClient authClient;

    private TextField usernameField;
    private PasswordField passwordField;
    private Button loginButton;
    private Button registerButton;
    private Label errorLabel;

    public LoginScreen(EventBus eventBus, AuthClient authClient) {
        this.eventBus = eventBus;
        this.authClient = authClient;
    }

    @Override
    protected void buildUI() {
        Styles.applyLogin(root);
        root.getStyleClass().add("login-root");

        Label titleLabel = new Label("Peerlock");
        titleLabel.getStyleClass().add("login-title");

        usernameField = new TextField();
        usernameField.setPromptText("Username");
        usernameField.getStyleClass().add("login-text-field");

        passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        passwordField.getStyleClass().add("login-text-field");

        loginButton = new Button("Login");
        loginButton.getStyleClass().add("primary-button");

        registerButton = new Button("Register");
        registerButton.getStyleClass().add("secondary-button");

        errorLabel = new Label();
        errorLabel.getStyleClass().add("error-label");


        HBox buttons = new HBox(12, loginButton, registerButton);
        buttons.setAlignment(Pos.CENTER);


        VBox formBox = new VBox(10, usernameField, passwordField, buttons, errorLabel);
        formBox.setAlignment(Pos.CENTER);
        formBox.setPadding(new Insets(20));
        formBox.getStyleClass().add("login-card");

        formBox.setMaxWidth(320);
        usernameField.setMaxWidth(Double.MAX_VALUE);
        passwordField.setMaxWidth(Double.MAX_VALUE);

        VBox centerBox = new VBox(16, titleLabel, formBox);
        centerBox.setAlignment(Pos.CENTER);

        BorderPane.setMargin(centerBox, new Insets(24));
        root.setCenter(centerBox);
    }

    @Override
    protected void registerListeners() {
        loginButton.setOnAction(e -> doLogin());
        registerButton.setOnAction(e -> doRegister());
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

                Platform.runLater(() -> eventBus.publish(
                        new AuthSuccessEvent(response.username(), response.accessToken())
                ));
            } catch (Exception ex) {
                Platform.runLater(() ->
                        errorLabel.setText("Login failed: " + ex.getMessage())
                );
            }
        }, "auth-login-thread").start();
    }

    private void doRegister() {
        String user = usernameField.getText();
        String pass = passwordField.getText();

        if (user == null || user.isBlank() || pass == null || pass.isBlank()) {
            errorLabel.setText("Please enter username and password.");
            return;
        }

        errorLabel.setText("Registering...");

        AuthRequest request = new AuthRequest(user, pass);

        new Thread(() -> {
            try {
                AuthResponse response = authClient.register(request);

                Platform.runLater(() -> eventBus.publish(
                        new AuthSuccessEvent(response.username(), response.accessToken())
                ));
            } catch (Exception ex) {
                Platform.runLater(() ->
                        errorLabel.setText("Registration failed: " + ex.getMessage())
                );
            }
        }, "auth-register-thread").start();
    }

    @Override
    protected void unregisterListeners() {
    }
}
