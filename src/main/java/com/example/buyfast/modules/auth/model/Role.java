package com.example.buyfast.modules.auth.model;

// --- MODIFIED IMPORT ---
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import java.sql.Timestamp;
import java.util.Set;
import java.util.UUID;

@Data
public class Role {
    @JsonIgnore
    private Long id;
    private UUID roleUuid;
    private String roleName;
    private String description;
    private String scope;
    @JsonIgnore
    private Long companyId;
    private Timestamp createdAt;
    private Set<Permission> permissions;
}