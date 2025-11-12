package com.example.buyfast.modules.auth.model;

import lombok.Data;
import java.sql.Timestamp;
import java.util.UUID;

/**
 * NEW MODEL
 * Defines a specific permission in the system (e.g., "MANAGE_PRODUCTS", "VIEW_USERS").
 * This fixes the "cannot find symbol: getPermissionName" error.
 */
@Data
public class Permission {
    private Long id;
    private UUID permissionUuid;
    private String permissionName; // The machine-readable name (e.g., "MANAGE_USERS")
    private String description;
    private Timestamp createdAt;
}