package com.example.buyfast.modules.chat.controller;
import com.example.buyfast.modules.chat.model.ChatMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.time.LocalDateTime;

@Controller
@RequiredArgsConstructor
public class ChatController {

    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Handles private messages.
     * Client sends JSON to: /app/chat.sendPrivateMessage
     */
    @MessageMapping("/chat.sendPrivateMessage")
    public void sendPrivateMessage(@Payload ChatMessage chatMessage) {
        // 1. Set timestamp
        chatMessage.setTimestamp(LocalDateTime.now().toString());

        // 2. TODO: Save 'chatMessage' to your Database here (Review/Chat Service)
        // chatService.save(chatMessage);

        // 3. Send to Recipient (The Seller)
        // Destination on client: /user/queue/messages
        messagingTemplate.convertAndSendToUser(
                chatMessage.getRecipientId(), // This MUST be the Seller's Email
                "/queue/messages",
                chatMessage
        );

        // 4. Send back to Sender (so they see their own message)
        // Destination on client: /user/queue/messages
        messagingTemplate.convertAndSendToUser(
                chatMessage.getSenderId(),    // This MUST be the Buyer's Email
                "/queue/messages",
                chatMessage
        );
    }
}