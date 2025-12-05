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
        // --- 1. DEBUG LOGS (Add these to see what is happening) ---
        System.out.println(">>> HIT CONTROLLER: sendPrivateMessage");

        if (principal == null) {
            System.out.println("ERROR: Principal is NULL. User not authenticated in WebSocket.");
            return;
        }

        System.out.println("1. Sender (Principal): " + principal.getName());
        System.out.println("2. Raw Recipient ID: " + chatMessage.getRecipientId());

        // --- 2. LOGIC ---
        String cleanRecipient = chatMessage.getRecipientId().toLowerCase().trim();
        chatMessage.setRecipientId(cleanRecipient);
        chatMessage.setTimestamp(LocalDateTime.now().toString());

        System.out.println("3. Sending to Clean Recipient: " + cleanRecipient);

        // --- 3. SENDING ---
        // Send to Recipient
        messagingTemplate.convertAndSendToUser(
                chatMessage.getRecipientId(),
                "/queue/messages",
                chatMessage
        );

        // Echo back to Sender
        messagingTemplate.convertAndSendToUser(
                chatMessage.getSenderId(),
                "/queue/messages",
                chatMessage
        );
        System.out.println(">>> MESSAGES SENT");
    }
}