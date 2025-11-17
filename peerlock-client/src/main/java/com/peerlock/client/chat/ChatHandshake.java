package com.peerlock.client.chat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;

public class ChatHandshake {

    public record Result(String remoteUsername, String remoteVersion) {}

    public static Result performClientHandshake(Socket socket, String localUsername, String clientVersion)
            throws IOException {

        PrintWriter out = new PrintWriter(
                new OutputStreamWriter(socket.getOutputStream()), true
        );
        BufferedReader in = new BufferedReader(
                new InputStreamReader(socket.getInputStream())
        );

        // Send HELLO
        out.println("HELLO|" + escape(localUsername) + "|" + escape(clientVersion));

        // Read response
        String line = in.readLine();
        if (line == null) {
            throw new IOException("Connection closed during handshake");
        }

        String[] parts = line.split("\\|", 3);
        String type = parts[0];

        if ("WELCOME".equals(type)) {
            String remoteUser = unescape(parts[1]);
            String remoteVersion = parts.length > 2 ? unescape(parts[2]) : "";
            return new Result(remoteUser, remoteVersion);
        } else if ("ERROR".equals(type)) {
            String code = parts.length > 1 ? unescape(parts[1]) : "UNKNOWN";
            String msg = parts.length > 2 ? unescape(parts[2]) : "";
            throw new IOException("Handshake error [" + code + "]: " + msg);
        } else {
            throw new IOException("Unexpected handshake response: " + line);
        }
    }

    public static Result performServerHandshake(Socket socket, String localUsername, String serverVersion)
            throws IOException {

        PrintWriter out = new PrintWriter(
                new OutputStreamWriter(socket.getOutputStream()), true
        );
        BufferedReader in = new BufferedReader(
                new InputStreamReader(socket.getInputStream())
        );

        String line = in.readLine();
        if (line == null) {
            throw new IOException("Connection closed before HELLO");
        }

        String[] parts = line.split("\\|", 3);
        String type = parts[0];

        if (!"HELLO".equals(type)) {
            out.println("ERROR|HANDSHAKE_FAILED|Expected HELLO");
            throw new IOException("Expected HELLO, got: " + line);
        }

        String remoteUser = unescape(parts[1]);
        String remoteVersion = parts.length > 2 ? unescape(parts[2]) : "";

        // TODO: validate version/username if needed

        out.println("WELCOME|" + escape(localUsername) + "|" + escape(serverVersion));
        return new Result(remoteUser, remoteVersion);
    }

    private static String escape(String s) {
        return s.replace("\\", "\\\\")
                .replace("|", "\\|")
                .replace("\n", "\\n");
    }

    private static String unescape(String s) {
        // simple unescape
        return s.replace("\\n", "\n")
                .replace("\\|", "|")
                .replace("\\\\", "\\");
    }
}
