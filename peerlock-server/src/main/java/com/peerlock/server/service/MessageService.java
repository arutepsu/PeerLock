package com.peerlock.server.service;

import java.time.Instant;
import java.util.List;

import org.springframework.stereotype.Service;

import com.peerlock.common.dto.ChatMessageDto;
import com.peerlock.server.domain.MessageEntity;
import com.peerlock.server.repository.MessageRepository;

@Service
public class MessageService {

    private final MessageRepository repo;

    public MessageService(MessageRepository repo) {
        this.repo = repo;
    }

    public ChatMessageDto save(String from, String to, String content, Instant timestamp) {
        Instant ts = (timestamp != null) ? timestamp : Instant.now();
        MessageEntity entity = new MessageEntity(from, to, content, ts);
        MessageEntity saved = repo.save(entity);
        return new ChatMessageDto(
                saved.getSenderUsername(),
                saved.getRecipientUsername(),
                saved.getContent(),
                saved.getSentAt()
        );
    }

    public List<ChatMessageDto> getConversationSince(String user1, String user2, Instant since) {
        return repo.findConversationSince(user1, user2, since).stream()
                .map(e -> new ChatMessageDto(
                        e.getSenderUsername(),
                        e.getRecipientUsername(),
                        e.getContent(),
                        e.getSentAt()
                ))
                .toList();
    }
}
