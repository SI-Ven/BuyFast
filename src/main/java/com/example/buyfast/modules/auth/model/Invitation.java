package com.example.buyfast.modules.auth.model;

import lombok.Data;
import java.sql.Timestamp;
import java.util.UUID;

/**
 * NEW MODEL
 * For the "enterprise" feature of inviting users to a company.
 */
@Data
public class Invitation {
    private Long id;
    private UUID invitationUuid;
    private String email;
    private String token;
    private Long companyId;
    private Long roleId; // The role to grant upon acceptance
    private String status; // e.g., "pending", "accepted", "expired"
    private Timestamp expiresAt;
    private Timestamp createdAt;
    private Timestamp updatedAt;
}