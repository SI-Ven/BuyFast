package com.example.buyfast.modules.chat.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChatMessage {
    private String senderId;      // Email of the buyer
    private String recipientId;   // Email of the seller
    private String productId;     // Product Context
    private String content;       // Message Text
    private String timestamp;
    private MessageType type;
}

