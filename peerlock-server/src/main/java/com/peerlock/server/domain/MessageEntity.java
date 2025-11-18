package com.peerlock.server.domain;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "messages")
public class MessageEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String senderUsername;
    private String recipientUsername;

    @Column(length = 4096)
    private String content;

    private Instant sentAt;

    protected MessageEntity() {
        // JPA requires a no-arg constructor
    }

    public MessageEntity(String senderUsername, String recipientUsername, String content, Instant sentAt) {
        this.senderUsername = senderUsername;
        this.recipientUsername = recipientUsername;
        this.content = content;
        this.sentAt = sentAt;
    }

    public Long getId() { return id; }

    public String getSenderUsername() { return senderUsername; }

    public String getRecipientUsername() { return recipientUsername; }

    public String getContent() { return content; }

    public Instant getSentAt() { return sentAt; }
}
