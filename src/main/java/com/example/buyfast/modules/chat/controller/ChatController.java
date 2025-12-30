package com.example.buyfast.modules.chat.controller;

import com.example.buyfast.modules.chat.model.ChatMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import java.time.LocalDateTime;
import java.security.Principal;

// ... existing imports

@Controller
@RequiredArgsConstructor
public class ChatController {

    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/chat.sendPrivateMessage")
    public void sendPrivateMessage(@Payload ChatMessage chatMessage, Principal principal) {
        if (principal == null) return;

        // principal.getName() now returns "resellkh@gmail.com" (buyer) or "povmeaa12@gmail.com" (seller)
        String senderEmail = principal.getName().toLowerCase().trim();
        String recipientEmail = chatMessage.getRecipientId().toLowerCase().trim();

        chatMessage.setSenderId(senderEmail);
        chatMessage.setRecipientId(recipientEmail);
        chatMessage.setTimestamp(LocalDateTime.now());

        // This sends to the recipient's personal queue
        // If recipient is 'resellkh@gmail.com', it sends to /user/resellkh@gmail.com/queue/messages
        messagingTemplate.convertAndSendToUser(
                recipientEmail,
                "/queue/messages",
                chatMessage
        );

        // Echo to the sender so they see their own message in the UI
        messagingTemplate.convertAndSendToUser(
                senderEmail,
                "/queue/messages",
                chatMessage
        );
    }
}