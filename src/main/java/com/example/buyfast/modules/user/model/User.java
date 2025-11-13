package com.example.buyfast.modules.user.model;

import com.example.buyfast.modules.auth.model.Role;
import com.fasterxml.jackson.annotation.JsonIgnore; // <-- IMPORT THIS
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.sql.Timestamp;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Data
public class User implements UserDetails {

    private Long id;
    private UUID userUuid;
    private String email;

    @JsonIgnore // <-- Hides "userPassword" from JSON
    private String userPassword;

    private String phoneNumber;
    private Boolean phoneVerified;
    private Boolean verified;
    private String status;
    private Long companyId;
    private Timestamp createdAt;
    private Timestamp lastLogin;

    private Set<Role> roles = new HashSet<>();
    private UserProfile userProfile;

    // --- UserDetails METHODS ---

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return roles.stream()
                .flatMap(role -> role.getPermissions().stream())
                .map(permission -> new SimpleGrantedAuthority(permission.getPermissionName()))
                .collect(Collectors.toList());
    }

    @Override
    @JsonIgnore // <-- Hides "password" from JSON
    public String getPassword() {
        return this.userPassword;
    }

    @Override
    @JsonIgnore // <-- Hides "username" (since you already have "email")
    public String getUsername() {
        return this.email;
    }

    @Override
    @JsonIgnore // <-- Hides "accountNonExpired"
    public boolean isAccountNonExpired() { return true; }

    @Override
    @JsonIgnore // <-- Hides "accountNonLocked"
    public boolean isAccountNonLocked() {
        return !"banned".equals(this.status);
    }

    @Override
    @JsonIgnore // <-- Hides "credentialsNonExpired"
    public boolean isCredentialsNonExpired() { return true; }

    @Override
    @JsonIgnore // <-- Hides "enabled"
    public boolean isEnabled() {
        return "active".equals(this.status);
    }
}