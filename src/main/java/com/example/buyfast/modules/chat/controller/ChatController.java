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
        if (principal == null) return;

        // 1. Force lowercase and trim to prevent "User@gmail.com" vs "user@gmail.com" bugs
        String senderEmail = principal.getName().toLowerCase().trim();
        String recipientEmail = chatMessage.getRecipientId().toLowerCase().trim();

        chatMessage.setSenderId(senderEmail);
        chatMessage.setRecipientId(recipientEmail);
        chatMessage.setTimestamp(LocalDateTime.now());

        // 2. Send to Recipient (The Buyer or Seller on the other end)
        // Spring looks for a session where Principal.getName() == recipientEmail
        messagingTemplate.convertAndSendToUser(
                recipientEmail,
                "/queue/messages",
                chatMessage
        );

        // 3. Send to Sender (Optional: syncs message across sender's open tabs)
        messagingTemplate.convertAndSendToUser(
                senderEmail,
                "/queue/messages",
                chatMessage
        );
    }
}
