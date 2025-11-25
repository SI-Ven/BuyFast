package com.example.buyfast.modules.user.model;

import com.example.buyfast.modules.auth.model.Role;
import com.fasterxml.jackson.annotation.JsonIgnore;
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
    @JsonIgnore
    private Long id;
    private UUID userUuid;
    private String email;

    @JsonIgnore
    private String userPassword;

    private String phoneNumber;
    private Boolean phoneVerified;
    private Boolean verified;
    private String status;
    private Long companyId;
    private Timestamp createdAt;
    private Timestamp lastLogin;

    // --- NEW FIELD ---
    @JsonIgnore
    private Integer tokenVersion = 0;

    private Set<Role> roles = new HashSet<>();
    private UserProfile userProfile;

    @Override
    @JsonIgnore
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return roles.stream()
                .flatMap(role -> role.getPermissions().stream())
                .map(permission -> new SimpleGrantedAuthority(permission.getPermissionName()))
                .collect(Collectors.toList());
    }

    @Override
    @JsonIgnore
    public String getPassword() {
        return this.userPassword;
    }

    @Override
    @JsonIgnore
    public String getUsername() {
        return this.email;
    }

    @Override
    @JsonIgnore
    public boolean isAccountNonExpired() { return true; }

    @Override
    @JsonIgnore
    public boolean isAccountNonLocked() {
        return !"banned".equals(this.status);
    }

    @Override
    @JsonIgnore
    public boolean isCredentialsNonExpired() { return true; }

    @Override
    @JsonIgnore
    public boolean isEnabled() {
        return "active".equals(this.status);
    }
}