package com.example.buyfast.modules.user.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User implements UserDetails {
    private Long id;
    private UUID publicId;
    private String username;
    private String email;

    @JsonIgnore
    private String password;

    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Builder.Default
    private List<Role> roles = new ArrayList<>();

    @Override
    @JsonIgnore
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (roles == null) return Collections.emptyList();
        Set<SimpleGrantedAuthority> authorities = new HashSet<>();
        for (Role role : roles) {
            if (role == null || !Boolean.TRUE.equals(role.getIsActive())) continue;
            authorities.add(new SimpleGrantedAuthority("ROLE_" + role.getName()));
            if (role.getPermissions() != null) {
                for (Permission p : role.getPermissions()) {
                    authorities.add(new SimpleGrantedAuthority(p.getPermissionKey()));
                }
            }
        }
        return authorities;
    }

    @Override
    @JsonIgnore
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    @JsonIgnore
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    @JsonIgnore
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    @JsonIgnore
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    @JsonIgnore
    public boolean isEnabled() {
        return isActive != null && isActive;
    }

    public void addRole(Role role) {
        if (roles == null) {
            roles = new ArrayList<>();
        }
        roles.add(role);
    }

    public void removeRole(Role role) {
        if (roles != null) {
            roles.remove(role);
        }
    }

    public boolean hasRole(String roleName) {
        if (roles == null || roleName == null) {
            return false;
        }
        return roles.stream()
                .anyMatch(role -> roleName.equals(role.getName()));
    }
}
