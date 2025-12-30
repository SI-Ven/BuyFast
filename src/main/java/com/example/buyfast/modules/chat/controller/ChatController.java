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

        // CRITICAL: Normalize every email to lowercase and trim
        String senderEmail = principal.getName().toLowerCase().trim();
        String recipientEmail = chatMessage.getRecipientId().toLowerCase().trim();

        chatMessage.setSenderId(senderEmail);
        chatMessage.setRecipientId(recipientEmail);
        chatMessage.setTimestamp(LocalDateTime.now());

        // Send to recipient
        messagingTemplate.convertAndSendToUser(
                recipientEmail,
                "/queue/messages",
                chatMessage
        );

        // Send to sender (echo)
        messagingTemplate.convertAndSendToUser(
                senderEmail,
                "/queue/messages",
                chatMessage
        );
    }
}