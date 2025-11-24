package com.example.buyfast.modules.user.service;

import com.example.buyfast.modules.user.dto.RegisterRequest;
import com.example.buyfast.modules.user.dto.UserCreateRequest;
import com.example.buyfast.modules.user.dto.UserResponse;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.List;
import java.util.UUID;

public interface UserService extends UserDetailsService {
    UserResponse registerUser(RegisterRequest request, String roleName);
    UserResponse createUser(UserCreateRequest request);
    UserResponse getUserById(Long id);
    UserResponse getUserByPublicId(UUID publicId);
    UserResponse getUserByUsername(String username);
    UserResponse getUserByEmail(String email);
    List<UserResponse> getAllUsers();
    List<UserResponse> getUsersByActiveStatus(Boolean isActive);
    UserResponse updateUser(Long id, RegisterRequest request);
    boolean updatePassword(Long id, String oldPassword, String newPassword);
    boolean deleteUser(Long id);
    boolean assignRoleToUser(Long userId, Integer roleId);
    boolean removeRoleFromUser(Long userId, Integer roleId);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    void enableUser(String email);
    void updatePassword(Long userId, String newPassword);
}