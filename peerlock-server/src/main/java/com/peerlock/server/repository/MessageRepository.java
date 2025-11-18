package com.peerlock.server.repository;

import java.time.Instant;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.peerlock.server.domain.MessageEntity;

public interface MessageRepository extends JpaRepository<MessageEntity, Long> {

    @Query("""
           SELECT m FROM MessageEntity m
           WHERE 
             ((m.senderUsername = :user1 AND m.recipientUsername = :user2) OR
              (m.senderUsername = :user2 AND m.recipientUsername = :user1))
             AND m.sentAt > :since
           ORDER BY m.sentAt ASC
           """)
    List<MessageEntity> findConversationSince(String user1, String user2, Instant since);
}
