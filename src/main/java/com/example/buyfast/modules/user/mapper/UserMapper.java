package com.example.buyfast.modules.user.mapper;

import com.example.buyfast.modules.user.dto.RegisterRequest;
import com.example.buyfast.modules.user.dto.UserCreateRequest;
import com.example.buyfast.modules.user.dto.UserResponse;
import com.example.buyfast.modules.user.dto.UserUpdateRequest;
import com.example.buyfast.modules.user.model.User;
import com.example.buyfast.modules.user.model.Permission; // Import Permission
import com.example.buyfast.modules.user.model.Role;       // Import Role
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.stream.Collectors;

@Component
public class UserMapper {

    public User toModel(RegisterRequest request) {
        if (request == null) {
            return null;
        }
        return User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .isActive(false) // Default to false until OTP verify
                .build();
    }

    public User toModel(UserCreateRequest request) {
        if (request == null) {
            return null;
        }
        return User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .build();
    }

    public UserResponse toResponse(User user) {
        if (user == null) {
            return null;
        }
        return UserResponse.builder()
                .publicId(user.getPublicId() != null ? user.getPublicId().toString() : null)
                .username(user.getUsername())
                .email(user.getEmail())
                .isActive(user.getIsActive())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())

                // 1. Map Role Names (e.g. ["ADMIN", "MANAGER"])
                .roles(user.getRoles() != null && !user.getRoles().isEmpty()
                        ? user.getRoles().stream()
                        .filter(role -> role != null && role.getName() != null)
                        .map(Role::getName)
                        .collect(Collectors.toList())
                        : Collections.emptyList())

                // 2. Map Permissions (e.g. ["user:read", "product:create"])
                .permissions(user.getRoles() != null && !user.getRoles().isEmpty()
                        ? user.getRoles().stream()
                        .filter(role -> role != null && role.getPermissions() != null) // Ensure permissions list exists
                        .flatMap(role -> role.getPermissions().stream())               // Flatten nested lists
                        .filter(p -> p != null && p.getPermissionKey() != null)        // Ensure permission is valid
                        .map(Permission::getPermissionKey)                             // Extract string key
                        .distinct()                                                    // Remove duplicates
                        .collect(Collectors.toList())
                        : Collections.emptyList())
                .build();
    }

    public void updateModel(User user, UserUpdateRequest request) {
        if (user == null || request == null) {
            return;
        }

        if (request.getUsername() != null) {
            user.setUsername(request.getUsername());
        }
        if (request.getEmail() != null) {
            user.setEmail(request.getEmail());
        }
        if (request.getIsActive() != null) {
            user.setIsActive(request.getIsActive());
        }
    }
}