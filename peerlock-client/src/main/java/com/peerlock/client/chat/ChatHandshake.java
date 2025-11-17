package com.peerlock.client.chat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.security.PublicKey;

import javax.crypto.SecretKey;

import com.peerlock.client.crypto.Crypto;

public class ChatHandshake {

    public record Result(String remoteUsername, String remoteVersion, SecretKey sharedKey) {}

    public static Result performClientHandshake(Socket socket, String localUsername, String clientVersion)
            throws IOException {

        var ecKeyPair = Crypto.generateEcKeyPair();
        String localPubB64 = Crypto.encodePublicKey(ecKeyPair.getPublic());

        PrintWriter out = new PrintWriter(
                new OutputStreamWriter(socket.getOutputStream()), true
        );
        BufferedReader in = new BufferedReader(
                new InputStreamReader(socket.getInputStream())
        );

        // Send HELLO|username|version|pubKey
        out.println("HELLO|" + escape(localUsername) + "|" + escape(clientVersion) + "|" + localPubB64);

        // Read response
        String line = in.readLine();
        if (line == null) {
            throw new IOException("Connection closed during handshake");
        }

        String[] parts = line.split("\\|", 4); // 4 parts: WELCOME|user|version|pubKey
        String type = parts[0];

        if ("WELCOME".equals(type)) {
            if (parts.length < 4) {
                throw new IOException("Invalid WELCOME message: " + line);
            }
            String remoteUser = unescape(parts[1]);
            String remoteVersion = unescape(parts[2]);
            String remotePubB64 = parts[3];

            PublicKey remotePub = Crypto.decodePublicKey(remotePubB64);
            SecretKey sharedKey = Crypto.deriveSharedKey(ecKeyPair.getPrivate(), remotePub);

            return new Result(remoteUser, remoteVersion, sharedKey);

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

        var ecKeyPair = Crypto.generateEcKeyPair();
        String localPubB64 = Crypto.encodePublicKey(ecKeyPair.getPublic());

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

        String[] parts = line.split("\\|", 4); // 4 parts: HELLO|user|version|pubKey
        String type = parts[0];

        if (!"HELLO".equals(type)) {
            out.println("ERROR|HANDSHAKE_FAILED|Expected HELLO");
            throw new IOException("Expected HELLO, got: " + line);
        }

        if (parts.length < 4) {
            out.println("ERROR|HANDSHAKE_FAILED|Invalid HELLO format");
            throw new IOException("Invalid HELLO message: " + line);
        }

        String remoteUser = unescape(parts[1]);
        String remoteVersion = unescape(parts[2]);
        String remotePubB64 = parts[3];

        PublicKey remotePub = Crypto.decodePublicKey(remotePubB64);
        SecretKey sharedKey = Crypto.deriveSharedKey(ecKeyPair.getPrivate(), remotePub);

        // Respond with WELCOME|username|version|pubKey
        out.println("WELCOME|" + escape(localUsername) + "|" + escape(serverVersion) + "|" + localPubB64);

        return new Result(remoteUser, remoteVersion, sharedKey);
    }

    private static String escape(String s) {
        return s.replace("\\", "\\\\")
                .replace("|", "\\|")
                .replace("\n", "\\n");
    }

    private static String unescape(String s) {
        // simple unescape (order matters!)
        return s.replace("\\n", "\n")
                .replace("\\|", "|")
                .replace("\\\\", "\\");
    }
}
