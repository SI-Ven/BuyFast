package com.example.buyfast.modules.auth.model;

import lombok.Data;
import java.sql.Timestamp;
import java.util.UUID;

@Data
public class Permission {
    private Long id;
    private UUID permissionUuid;
    private String permissionName;
    private String description;
    private String resourceGroup; // <--- Added to match DB Schema
    private Timestamp createdAt;
}