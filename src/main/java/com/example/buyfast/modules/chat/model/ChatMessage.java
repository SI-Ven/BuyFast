package com.example.buyfast.modules.chat.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChatMessage {
    private String id;
    private String senderId;
    private String recipientId;
    private String content;
    private String imageUrl; // ADD THIS LINE
    private String productId;
    private LocalDateTime timestamp;
    private MessageType type;
}

