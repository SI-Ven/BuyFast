package com.example.buyfast.modules.verify.model;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class Verify {
    private Long id;
    private UUID verifyUuid;
    private String targetType; // e.g., "user"
    private UUID targetId;     // The user's user_uuid
    private Long submittedBy;  // The user's internal id
    private String verifyDocuments; // Will store JSON: {"id_card_url": "http://..."}
    private String status;     // 'pending', 'approved', 'rejected'
    private Long reviewedBy;   // The admin's internal id
    private LocalDateTime reviewedAt;
    private LocalDateTime createdAt;
    private String remarks;
}