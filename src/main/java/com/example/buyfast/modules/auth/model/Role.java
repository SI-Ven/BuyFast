package com.example.buyfast.modules.auth.model;

// --- MODIFIED IMPORT ---
import com.example.buyfast.modules.auth.model.Permission; // <-- Use your new model
import lombok.Data;

// import java.security.Permission; // <-- REMOVED
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
    // --- MODIFIED TYPE ---
    private Set<Permission> permissions; // <-- Use your new model
}