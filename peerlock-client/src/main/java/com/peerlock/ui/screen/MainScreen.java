package com.peerlock.ui.screen;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import com.peerlock.client.chat.history.FileMessageStore;
import com.peerlock.client.chat.history.HttpMessageHistoryClient;
import com.peerlock.client.chat.history.MessageHistorySyncService;
import com.peerlock.client.chat.history.MessageStore;
import com.peerlock.client.model.MessageHistoryClient;
import com.peerlock.client.p2p.PeerSocketClient;
import com.peerlock.client.p2p.PeerSocketServer;
import com.peerlock.client.peer.PeerClient;
import com.peerlock.common.dto.ChatMessageDto;
import com.peerlock.common.model.PeerInfo;
import com.peerlock.ui.base.BaseScreen;
import com.peerlock.ui.event.EventBus;
import com.peerlock.ui.event.IncomingMessageEvent;
import com.peerlock.ui.event.LogoutEvent;
import com.peerlock.ui.utils.Styles;

import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;

public class MainScreen extends BaseScreen {

    private final EventBus eventBus;
    private final String currentUser;
    private final String accessToken;
    private final PeerClient peerClient;
    private final int listeningPort;

    private final PeerSocketServer socketServer;
    private final PeerSocketClient socketClient;
    private final ExecutorService ioExecutor = Executors.newCachedThreadPool();
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private final MessageStore messageStore;
    private final MessageHistorySyncService historySyncService;

    private Label headerLabel;
    private ListView<PeerInfo> peersList;
    private TextArea chatArea;
    private TextField messageField;
    private Button sendButton;
    private Button logoutButton;
    
    private Consumer<IncomingMessageEvent> incomingMessageListener;

    // currently opened chat peer (for showing history + messages)
    private PeerInfo currentChatPeer;

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
        this.messageStore = new FileMessageStore(currentUser);
        MessageHistoryClient historyClient =
            new HttpMessageHistoryClient("http://localhost:8080/api/v1/messages");
        this.historySyncService = new MessageHistorySyncService(messageStore, historyClient, accessToken);
        this.socketServer = new PeerSocketServer(
                listeningPort,
                eventBus,
                currentUser,
                messageStore,
                historySyncService
        );
        this.socketClient = new PeerSocketClient(currentUser, messageStore, historySyncService);
    }

    @Override
    protected void buildUI() {
        Styles.applyMain(root);
        root.getStyleClass().add("main-root");

        headerLabel = new Label("PeerLock • Logged in as " + currentUser + " (port " + listeningPort + ")");
        headerLabel.getStyleClass().add("header-label");

        logoutButton = new Button("Logout");
        logoutButton.getStyleClass().add("logout-button");

        HBox headerBar = new HBox(10, headerLabel, logoutButton);
        headerBar.getStyleClass().add("header-bar");

        peersList = new ListView<>();
        peersList.getStyleClass().add("peers-list");
        peersList.setPlaceholder(new Label("No peers online"));
        peersList.setPrefWidth(220);

        peersList.setCellFactory(listView -> new ListCell<>() {
            @Override
            protected void updateItem(PeerInfo item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.username() + " (" + item.host() + ":" + item.port() + ")");
                }
                getStyleClass().add("list-cell");
            }
        });

        chatArea = new TextArea();
        chatArea.setEditable(false);
        chatArea.setWrapText(true);
        chatArea.getStyleClass().add("chat-area");

        messageField = new TextField();
        messageField.setPromptText("Type a message...");
        messageField.getStyleClass().add("message-field");

        sendButton = new Button("Send");
        sendButton.getStyleClass().add("send-button");

        HBox inputBar = new HBox(8, messageField, sendButton);
        inputBar.getStyleClass().add("input-bar");

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

        logoutButton.setOnAction(e -> eventBus.publish(new LogoutEvent(currentUser, accessToken)));

        incomingMessageListener = event ->
                Platform.runLater(() ->
                        chatArea.appendText("[" + event.getFromUsername() + " → me]: " + event.getMessage() + "\n")
                );

        eventBus.subscribe(IncomingMessageEvent.class, incomingMessageListener);


        peersList.getSelectionModel().selectedItemProperty().addListener((obs, oldPeer, newPeer) -> {
            currentChatPeer = newPeer;
            if (newPeer != null) {
                loadHistoryForPeer(newPeer);
            } else {
                chatArea.clear();
            }
        });

        socketServer.start();
        sendHeartbeatAsync();
        loadPeersAsync();

        scheduler.scheduleAtFixedRate(
                this::loadPeersAsync,
                5,
                10,
                TimeUnit.SECONDS
        );
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

        ioExecutor.submit(() -> {
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
        });
    }


    /**
     * Load local history + sync with server for the selected peer.
     */
    private void loadHistoryForPeer(PeerInfo peer) {
        chatArea.clear();
        showInfo("Loading history with " + peer.username() + "...");

        ioExecutor.submit(() -> {
            try {
                List<ChatMessageDto> messages = historySyncService.loadAndSync(peer.username());
                Platform.runLater(() -> {
                    chatArea.clear();
                    for (ChatMessageDto msg : messages) {
                        appendMessageToChatArea(msg);
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() ->
                        showInfo("Failed to load history with " + peer.username() + ": " + e.getMessage())
                );
            }
        });
    }

    private void appendMessageToChatArea(ChatMessageDto msg) {
        String direction;
        if (msg.from().equals(currentUser)) {
            direction = currentUser + " → " + msg.to();
        } else {
            direction = msg.from() + " → " + currentUser;
        }
        chatArea.appendText("[" + direction + "]: " + msg.content() + "\n");
    }

    private void sendHeartbeatAsync() {
        ioExecutor.submit(() -> {
            try {
                peerClient.sendHeartbeat(accessToken, listeningPort);
                showInfo("Heartbeat sent on port " + listeningPort);
            } catch (Exception e) {
                e.printStackTrace();
                showInfo("Heartbeat failed: " + e.getMessage());
            }
        });
    }

    private void loadPeersAsync() {
        ioExecutor.submit(() -> {
            try {
                List<PeerInfo> peers = peerClient.getOnlinePeers(accessToken);
                Platform.runLater(() -> peersList.getItems().setAll(peers));
            } catch (Exception e) {
                e.printStackTrace();
                showInfo("Failed to load peers: " + e.getMessage());
            }
        });
    }

    private void showInfo(String msg) {
        if (Platform.isFxApplicationThread()) {
            chatArea.appendText("[INFO] " + msg + "\n");
        } else {
            Platform.runLater(() -> chatArea.appendText("[INFO] " + msg + "\n"));
        }
    }

    @Override
    protected void unregisterListeners() {
        if (incomingMessageListener != null) {
            eventBus.unsubscribe(IncomingMessageEvent.class, incomingMessageListener);
        }
        socketServer.stop();
        ioExecutor.shutdownNow();
        scheduler.shutdownNow();
    }
}
