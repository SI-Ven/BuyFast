package com.example.buyfast.modules.chat.controller;

import lombok.Data;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
public class ChatController {

    @MessageMapping("/chat.sendMessage")
    @SendTo("/topic/public")
    public ChatMessage sendMessage(@Payload ChatMessage chatMessage) {
        // Save to DB here (impl omitted for brevity)
        return chatMessage;
    }

    @Data
    public static class ChatMessage {
        private String sender;
        private String content;
        private MessageType type;
    }

    public enum MessageType { CHAT, JOIN, LEAVE }
}