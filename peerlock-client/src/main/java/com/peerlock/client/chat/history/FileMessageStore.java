package com.peerlock.client.chat.history;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.peerlock.client.chat.ChatMessage;


public class FileMessageStore implements MessageStore {

    private final Path baseDir;
    private final String localUsername;
    private final ObjectMapper mapper = new ObjectMapper();

    public FileMessageStore(String localUsername) {
        this.localUsername = localUsername;
        this.baseDir = Paths.get(System.getProperty("user.home"), ".peerlock", "history", localUsername);
    }

    @Override
    public void appendMessage(ChatMessage message) {
        try {
            Path file = getFileFor(message.from(), message.to());
            Files.createDirectories(file.getParent());

            try (BufferedWriter writer = Files.newBufferedWriter(
                    file, StandardOpenOption.CREATE, StandardOpenOption.APPEND
            )) {
                writer.write(mapper.writeValueAsString(message));
                writer.newLine();
            }
        } catch (IOException e) {
            // TODO log in real app
            e.printStackTrace();
        }
    }

    @Override
    public List<ChatMessage> getHistoryWith(String otherUsername) {
        Path file = getFileFor(localUsername, otherUsername);
        if (!Files.exists(file)) return List.of();

        List<ChatMessage> result = new ArrayList<>();
        try (BufferedReader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            String line;
            while ((line = reader.readLine()) != null) {
                ChatMessage msg = mapper.readValue(line, ChatMessage.class);
                result.add(msg);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    @Override
    public void clearHistoryWith(String otherUsername) {
        Path file = getFileFor(localUsername, otherUsername);
        try {
            Files.deleteIfExists(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Store history per "local ↔ other" conversation.
     * File name is based on the *other* user, so both directions end up in the same file.
     */
    private Path getFileFor(String from, String to) {
        String other = from.equals(localUsername) ? to : from;
        return baseDir.resolve(other + ".jsonl");
    }
}
