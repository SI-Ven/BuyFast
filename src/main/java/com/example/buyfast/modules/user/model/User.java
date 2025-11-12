package com.example.buyfast.modules.user.model;

import com.example.buyfast.modules.auth.model.Role;
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

    // --- Fields from 'users' table (Auth & Status) ---
    private Long id;
    private UUID userUuid;
    private String email;
    private String userPassword; // Mapped to user_password in DB
    private String phoneNumber;
    private Boolean phoneVerified;
    private Boolean verified;
    private String status;
    private Long companyId;
    private Timestamp createdAt;
    private Timestamp lastLogin;

    // --- REMOVED PROFILE FIELDS (Moved to UserProfile) ---
    // private String firstName;
    // private String lastName;
    // private String userName;
    // private String userProfile;
    // private Date dob;
    // private String address;

    // --- REMOVED 'role' string ---
    // private String role;

    // --- NEW RELATIONSHIPS (Populated by MyBatis) ---
    private Set<Role> roles = new HashSet<>();

    // --- UserDetails METHODS (CRITICAL UPDATE) ---

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // This is the most important change.
        // We now return a list of PERMISSIONS, not roles.
        // Spring Security will check these with .hasAuthority()
        return roles.stream()
                .flatMap(role -> role.getPermissions().stream())
                .map(permission -> new SimpleGrantedAuthority(permission.getPermissionName()))
                .collect(Collectors.toList());
    }

    @Override
    public String getPassword() {
        return this.userPassword;
    }

    @Override
    public String getUsername() {
        return this.email; // Use email for username
    }

    @Override
    public boolean isAccountNonExpired() { return true; }

    @Override
    public boolean isAccountNonLocked() {
        return !"banned".equals(this.status);
    }

    @Override
    public boolean isCredentialsNonExpired() { return true; }

    @Override
    public boolean isEnabled() {
        return "active".equals(this.status);
    }
}