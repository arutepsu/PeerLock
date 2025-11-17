package com.peerlock.ui.screen;

import com.peerlock.client.p2p.PeerSocketClient;
import com.peerlock.client.p2p.PeerSocketServer;
import com.peerlock.client.peer.PeerClient;
import com.peerlock.common.model.PeerInfo;
import com.peerlock.ui.base.BaseScreen;
import com.peerlock.ui.event.EventBus;
import com.peerlock.ui.event.IncomingMessageEvent;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;

import java.util.List;
import java.util.function.Consumer;

public class MainScreen extends BaseScreen {

    private final EventBus eventBus;
    private final String currentUser;
    private final String accessToken;
    private final PeerClient peerClient;
    private final int listeningPort;

    private final PeerSocketServer socketServer;
    private final PeerSocketClient socketClient;

    // UI
    private Label headerLabel;
    private ListView<PeerInfo> peersList;
    private TextArea chatArea;
    private TextField messageField;
    private Button sendButton;
    private Button logoutButton;

    // EventBus listener
    private Consumer<IncomingMessageEvent> incomingMessageListener;

    public MainScreen(EventBus eventBus,
                      String currentUser,
                      String accessToken,
                      PeerClient peerClient,
                      int listeningPort) {
        this.eventBus = eventBus;
        this.currentUser = currentUser;
        this.accessToken = accessToken;
        this.peerClient = peerClient;
        this.listeningPort = listeningPort;

        this.socketServer = new PeerSocketServer(listeningPort, eventBus);
        this.socketClient = new PeerSocketClient(currentUser);
    }

    @Override
    protected void buildUI() {
        // header
        headerLabel = new Label("PeerLock • Logged in as " + currentUser + " (listening on " + listeningPort + ")");
        logoutButton = new Button("Logout");

        HBox headerBar = new HBox(10, headerLabel, logoutButton);
        headerBar.setPadding(new Insets(10));

        // peers list
        peersList = new ListView<>();
        peersList.setPlaceholder(new Label("No peers online yet"));
        peersList.setPrefWidth(220);

        // pretty display: show "username (host:port)"
        peersList.setCellFactory(listView -> new ListCell<>() {
            @Override
            protected void updateItem(PeerInfo item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.username() + " (" + item.host() + ":" + item.port() + ")");
                }
            }
        });

        // chat area
        chatArea = new TextArea();
        chatArea.setEditable(false);
        chatArea.setWrapText(true);

        // message input
        messageField = new TextField();
        messageField.setPromptText("Type a message...");
        sendButton = new Button("Send");

        HBox inputBar = new HBox(8, messageField, sendButton);
        inputBar.setPadding(new Insets(8));

        BorderPane centerPane = new BorderPane();
        centerPane.setCenter(chatArea);
        centerPane.setBottom(inputBar);

        root.setTop(headerBar);
        root.setLeft(peersList);
        root.setCenter(centerPane);

        // start listening for incoming messages
        socketServer.start();
        sendHeartbeatAsync();
        // TODO: send heartbeat to backend here with (accessToken, listeningPort)

        loadPeersAsync();
    }

    @Override
    protected void registerListeners() {
        sendButton.setOnAction(e -> sendMessage());
        messageField.setOnAction(e -> sendMessage());

        logoutButton.setOnAction(e -> {
            // TODO: publish LogoutEvent to go back to LoginScreen
            chatArea.clear();
            peersList.getItems().clear();
        });

        incomingMessageListener = event -> {
            chatArea.appendText("[" + event.getFromUsername() + " → me]: " + event.getMessage() + "\n");
        };
        eventBus.subscribe(IncomingMessageEvent.class, incomingMessageListener);
    }

    private void sendMessage() {
        PeerInfo target = peersList.getSelectionModel().getSelectedItem();
        if (target == null) {
            showInfo("Please select a peer first.");
            return;
        }

        String text = messageField.getText();
        if (text == null || text.isBlank()) {
            return;
        }

        messageField.clear();

        // send over TCP on a background thread
        new Thread(() -> {
            try {
                socketClient.sendMessage(target, text);
                Platform.runLater(() ->
                        chatArea.appendText("[" + currentUser + " → " + target.username() + "]: " + text + "\n")
                );
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() ->
                        showInfo("Failed to send message to " + target.username() + ": " + e.getMessage())
                );
            }
        }, "send-message-to-" + target.username()).start();
    }
    
    private void sendHeartbeatAsync() {
        new Thread(() -> {
            try {
                peerClient.sendHeartbeat(accessToken, listeningPort);
                Platform.runLater(() ->
                        chatArea.appendText(
                                "[INFO] Heartbeat sent on port " + listeningPort + "\n")
                );
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() ->
                        chatArea.appendText(
                                "[ERROR] Heartbeat failed: " + e.getMessage() + "\n")
                );
            }
        }, "heartbeat-thread").start();
    }

    private void loadPeersAsync() {
        new Thread(() -> {
            try {
                List<PeerInfo> peers = peerClient.getOnlinePeers(accessToken);
                Platform.runLater(() -> peersList.getItems().setAll(peers));
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() ->
                        showInfo("Failed to load peers: " + e.getMessage())
                );
            }
        }, "load-peers-thread").start();
    }

    private void showInfo(String msg) {
        // simple: write into chatArea, or you can use a dialog
        chatArea.appendText("[INFO] " + msg + "\n");
    }

    @Override
    protected void unregisterListeners() {
        if (incomingMessageListener != null) {
            eventBus.unsubscribe(IncomingMessageEvent.class, incomingMessageListener);
        }
        socketServer.stop();
    }
}
