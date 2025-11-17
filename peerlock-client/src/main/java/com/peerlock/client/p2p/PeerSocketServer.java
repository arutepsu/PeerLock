package com.peerlock.client.p2p;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

import com.peerlock.ui.event.EventBus;
import com.peerlock.ui.event.IncomingMessageEvent;

/**
 * Simple TCP server that accepts connections from peers.
 * Protocol:
 *  - first line: sender username
 *  - subsequent lines: messages
 */
public class PeerSocketServer {

    private final int port;
    private final EventBus eventBus;

    private volatile boolean running = false;
    private Thread serverThread;
    private ServerSocket serverSocket;

    public PeerSocketServer(int port, EventBus eventBus) {
        this.port = port;
        this.eventBus = eventBus;
    }

    public void start() {
        if (running) {
            return;
        }
        running = true;

        serverThread = new Thread(this::runServer, "peer-socket-server-" + port);
        serverThread.setDaemon(true);
        serverThread.start();
    }

    public void stop() {
        running = false;
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (Exception ignored) {
        }
        if (serverThread != null) {
            serverThread.interrupt();
        }
    }

    private void runServer() {
        try (ServerSocket ss = new ServerSocket(port)) {
            this.serverSocket = ss;

            while (running) {
                Socket socket = ss.accept();
                handleClient(socket);
            }
        } catch (Exception e) {
            if (running) {
                e.printStackTrace(); // TODO: proper logging
            }
        }
    }

    private void handleClient(Socket socket) {
        Thread clientThread = new Thread(() -> {
            try (socket;
                 BufferedReader reader = new BufferedReader(
                         new InputStreamReader(socket.getInputStream()))) {

                // first line: who is sending
                String fromUser = reader.readLine();
                if (fromUser == null) {
                    return;
                }

                String line;
                while ((line = reader.readLine()) != null) {
                    eventBus.publish(new IncomingMessageEvent(fromUser, line));
                }
            } catch (Exception e) {
                // TODO: logging
            }
        }, "peer-client-handler-" + socket.getRemoteSocketAddress());

        clientThread.setDaemon(true);
        clientThread.start();
    }
}
