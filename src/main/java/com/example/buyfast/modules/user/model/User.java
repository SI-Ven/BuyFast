package com.example.buyfast.modules.user.model;

import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Data
public class User implements UserDetails {

    // Fields from your Schema.sql
    private Long id;
    private UUID userUuid;
    private String firstName;
    private String lastName;
    private String userName;
    private String userProfile;
    private LocalDate dob;
    private String address;
    private String email;
    private String userPassword; // This is the hashed password
    private String phoneNumber;
    private boolean phoneVerified;
    private boolean verified;
    private String role;
    private Long companyId; // <-- THIS IS THE NEW FIELD THAT WAS MISSING
    private String status; // 'pending', 'active', 'banned', etc.
    private LocalDateTime createdAt;
    private LocalDateTime lastLogin;

    // --- UserDetails Implementation ---

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Use the 'role' field for permissions
        return List.of(new SimpleGrantedAuthority(role));
    }

    @Override
    public String getPassword() {
        // Field from DB containing the hashed password
        return userPassword;
    }

    @Override
    public String getUsername() {
        // We use email as the unique username
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        // This is the key: Spring Security will check this.
        // If status is not 'active', login will fail with DisabledException.
        return "active".equals(status);
    }
}