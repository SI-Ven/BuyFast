package com.example.buyfast.modules.chat.controller;

import com.example.buyfast.modules.chat.model.ChatMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.time.LocalDateTime;
import java.security.Principal;

@Controller
@RequiredArgsConstructor
public class ChatController {

    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/chat.sendPrivateMessage")
    public void sendPrivateMessage(@Payload ChatMessage chatMessage, Principal principal) {
        // 1. Validation & Security
        if (principal == null) {
            System.out.println("ERROR: Unauthorized WebSocket Attempt.");
            return;
        }

        // 2. Set official server-side metadata
        // Ensure senderId is actually the authenticated user to prevent spoofing
        chatMessage.setSenderId(principal.getName());
        chatMessage.setTimestamp(LocalDateTime.now());

        // Clean up recipient ID
        if (chatMessage.getRecipientId() != null) {
            chatMessage.setRecipientId(chatMessage.getRecipientId().toLowerCase().trim());
        }

        // Debug Log to see if imageUrl is coming through
        System.out.println("Message from: " + chatMessage.getSenderId());
        System.out.println("Content: " + chatMessage.getContent());
        System.out.println("Image URL: " + chatMessage.getImageUrl());

        // 3. Dispatch to Recipient
        // This sends to the destination: /user/{recipientId}/queue/messages
        messagingTemplate.convertAndSendToUser(
                chatMessage.getRecipientId(),
                "/queue/messages",
                chatMessage
        );

        // 4. Echo back to Sender (so their UI updates instantly)
        messagingTemplate.convertAndSendToUser(
                chatMessage.getSenderId(),
                "/queue/messages",
                chatMessage
        );
    }
}