package com.peerlock.server.controller;

import java.time.Instant;
import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.peerlock.common.dto.ChatMessageDto;
import com.peerlock.server.messages.SendMessageRequest;
import com.peerlock.server.service.MessageService;

@RestController
@RequestMapping("/api/v1/messages")
public class MessageController {

    private final MessageService service;

    public MessageController(MessageService service) {
        this.service = service;
    }

    @PostMapping
    public ChatMessageDto storeMessage(
            Authentication authentication,
            @RequestBody SendMessageRequest request
    ) {
        String from = authentication.getName();
        return service.save(from, request.to(), request.content(), request.timestamp());
    }

    @GetMapping("/with/{otherUsername}")
    public List<ChatMessageDto> getConversation(
            Authentication authentication,
            @PathVariable String otherUsername,
            @RequestParam(name = "since", required = false) String sinceRaw
    ) {
        String currentUser = authentication.getName();
        Instant since = (sinceRaw == null || sinceRaw.isBlank())
                ? Instant.EPOCH
                : Instant.parse(sinceRaw);

        return service.getConversationSince(currentUser, otherUsername, since);
    }
}
