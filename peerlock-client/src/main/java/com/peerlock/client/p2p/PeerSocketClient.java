package com.peerlock.client.p2p;

import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.net.Socket;

import com.peerlock.common.model.PeerInfo;


/**
 * Simple TCP client for sending messages to a peer.
 */
public class PeerSocketClient {

    private final String currentUsername;

    public PeerSocketClient(String currentUsername) {
        this.currentUsername = currentUsername;
    }

    public void sendMessage(PeerInfo target, String message) throws Exception {
        sendMessage(target.host(), target.port(), message);
    }

    public void sendMessage(String host, int port, String message) throws Exception {
        try (Socket socket = new Socket(host, port);
             BufferedWriter writer = new BufferedWriter(
                     new OutputStreamWriter(socket.getOutputStream()))) {

            // first line: who am I
            writer.write(currentUsername);
            writer.newLine();

            // second line: message
            writer.write(message);
            writer.newLine();
            writer.flush();
        }
    }
}
