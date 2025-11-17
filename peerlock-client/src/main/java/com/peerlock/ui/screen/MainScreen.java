package com.peerlock.ui.screen;

import com.peerlock.ui.base.BaseScreen;
import com.peerlock.ui.event.EventBus;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;

public class MainScreen extends BaseScreen {

    private final EventBus eventBus;
    private final String currentUser;

    private Label headerLabel;
    private ListView<String> peersList;
    private TextArea chatArea;
    private TextField messageField;
    private Button sendButton;
    private Button logoutButton;

    public MainScreen(EventBus eventBus, String currentUser) {
        this.eventBus = eventBus;
        this.currentUser = currentUser;
    }

    @Override
    protected void buildUI() {
        headerLabel = new Label("PeerLock • Logged in as " + currentUser);
        logoutButton = new Button("Logout");

        HBox headerBar = new HBox(10, headerLabel, logoutButton);

        peersList = new ListView<>();
        peersList.setPlaceholder(new Label("No peers online yet"));
        peersList.setPrefWidth(180);

        chatArea = new TextArea();
        chatArea.setEditable(false);
        chatArea.setWrapText(true);

        messageField = new TextField();
        messageField.setPromptText("Type a message...");
        sendButton = new Button("Send");

        HBox inputBar = new HBox(8, messageField, sendButton);

        BorderPane centerPane = new BorderPane();
        centerPane.setCenter(chatArea);
        centerPane.setBottom(inputBar);

        root.setTop(headerBar);
        root.setLeft(peersList);
        root.setCenter(centerPane);
    }

    @Override
    protected void registerListeners() {
        sendButton.setOnAction(e -> sendMessage());
        messageField.setOnAction(e -> sendMessage());

        logoutButton.setOnAction(e -> {
            // TODO: eventBus.publish(new LogoutEvent(currentUser));
            chatArea.clear();
            peersList.getItems().clear();
        });

        // TODO later: subscribe to PeerMessageReceivedEvent, PeersUpdatedEvent, etc.
    }

    private void sendMessage() {
        String message = messageField.getText();
        if (message == null || message.isBlank()) {
            return;
        }

        // TODO: use authToken + selected peer to send via PeerService/PeerClient
        chatArea.appendText("[" + currentUser + "]: " + message + "\n");
        messageField.clear();
    }

    @Override
    protected void unregisterListeners() {
        // when you add EventBus subscriptions here, unsubscribe them
    }
}
