package com.example.buyfast.modules.auth.model;

import lombok.Data;

import java.security.Permission;
import java.sql.Timestamp;
import java.util.Set;
import java.util.UUID;

@Data
public class Role {
    private Long id;
    private UUID roleUuid;
    private String roleName;
    private String description;
    private String scope;
    private Long companyId;
    private Timestamp createdAt;

    // This will be populated by our complex MyBatis query
    private Set<Permission> permissions;
}