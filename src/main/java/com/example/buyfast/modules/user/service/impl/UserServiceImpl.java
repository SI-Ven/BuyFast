package com.example.buyfast.modules.user.service.impl;

import com.example.buyfast.modules.user.dto.RegisterRequest;
import com.example.buyfast.modules.user.dto.UserCreateRequest;
import com.example.buyfast.modules.user.dto.UserResponse;
import com.example.buyfast.modules.user.mapper.UserMapper;
import com.example.buyfast.modules.user.model.Role;
import com.example.buyfast.modules.user.model.User;
import com.example.buyfast.modules.user.repository.RoleRepository;
import com.example.buyfast.modules.user.repository.UserRepository;
import com.example.buyfast.modules.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;

    @Override
    @Transactional
    public UserResponse registerUser(RegisterRequest request, String roleName) {
        log.info("Register user with email: {}", request.getEmail());
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already exists: " + request.getEmail());
        }
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Username already exists: " + request.getUsername());
        }
        Role userRole = roleRepository.findByName(roleName)
                .orElseThrow(() -> new RuntimeException("Role not found: " + roleName));
        User user = userMapper.toModel(request);
        user.setPublicId(UUID.randomUUID());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setIsActive(false);
        int result = userRepository.insert(user);
        if (result == 0) {
            throw new RuntimeException("Failed to create user");
        }
        roleRepository.addRoleToUser(user.getId(), userRole.getRoleId());
        log.info("Register successfully with email: {}", request.getEmail());
        User finalUser = userRepository.findById(user.getId())
                .orElseThrow(() -> new UsernameNotFoundException("User created but not found: " + request.getEmail()));
        return userMapper.toResponse(finalUser);
    }

    @Override
    @Transactional
    public UserResponse createUser(UserCreateRequest request) {
        log.info("Creating new user (by Admin) with username: {}", request.getUsername());

        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Username already exists: " + request.getUsername());
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already exists: " + request.getEmail());
        }

        User user = userMapper.toModel(request);
        user.setPublicId(UUID.randomUUID());
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        int result = userRepository.insert(user);
        if (result == 0) {
            throw new RuntimeException("Failed to create user");
        }

        if (request.getRoleName() != null && !request.getRoleName().isEmpty()) {
            Role userRole = roleRepository.findByName(request.getRoleName())
                    .orElseThrow(() -> new RuntimeException("Role not found: " + request.getRoleName()));
            roleRepository.addRoleToUser(user.getId(), userRole.getRoleId());
        }

        User finalUser = userRepository.findById(user.getId())
                .orElseThrow(() -> new RuntimeException("User created but not found"));

        log.info("User created successfully by admin with ID: {}", finalUser.getId());
        return userMapper.toResponse(finalUser);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {
        log.debug("Fetching user by ID: {}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + id));
        return userMapper.toResponse(user);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserByPublicId(UUID publicId) {
        log.debug("Fetching user by public ID: {}", publicId);
        User user = userRepository.findByPublicId(publicId)
                .orElseThrow(() -> new RuntimeException("User not found with public ID: " + publicId));
        return userMapper.toResponse(user);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserByUsername(String username) {
        log.debug("Fetching user by username: {}", username);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found with username: " + username));
        return userMapper.toResponse(user);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserByEmail(String email) {
        log.debug("Fetching user by email: {}", email);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
        return userMapper.toResponse(user);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        log.debug("Fetching all users");
        List<User> users = userRepository.findAll();
        return users.stream()
                .map(userMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> getUsersByActiveStatus(Boolean isActive) {
        log.debug("Fetching users by active status: {}", isActive);
        List<User> users = userRepository.findByIsActive(isActive);
        return users.stream()
                .map(userMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public UserResponse updateUser(Long id, RegisterRequest request) {
        log.info("Updating user with ID: {}", id);

        // Check if user exists
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + id));

        // Validate username uniqueness (if changed)
        if (!existingUser.getUsername().equals(request.getUsername())
                && userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Username already exists: " + request.getUsername());
        }

        // Validate email uniqueness (if changed)
        if (!existingUser.getEmail().equals(request.getEmail())
                && userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already exists: " + request.getEmail());
        }

        // Update fields
        existingUser.setUsername(request.getUsername());
        existingUser.setEmail(request.getEmail());
//        existingUser.setUpdatedAt(LocalDateTime.now());

        // Save updates
        int result = userRepository.update(existingUser);
        if (result == 0) {
            throw new RuntimeException("Failed to update user");
        }

        // Fetch and return updated user
        User updatedUser = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User updated but not found"));

        log.info("User updated successfully with ID: {}", id);
        return userMapper.toResponse(updatedUser);
    }

    @Override
    @Transactional
    public boolean updatePassword(Long id, String oldPassword, String newPassword) {
        log.info("Updating password for user ID: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + id));

        // Verify old password
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new IllegalArgumentException("Current password is incorrect");
        }

        // Encode and update new password
        String encodedPassword = passwordEncoder.encode(newPassword);
        int result = userRepository.updatePassword(id, encodedPassword);

        log.info("Password updated successfully for user ID: {}", id);
        return result > 0;
    }

    @Override
    @Transactional
    public boolean deleteUser(Long id) {
        log.info("Deleting user with ID: {}", id);

        // Check if user exists
        userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + id));

        // Remove all role associations first
        roleRepository.removeAllRolesFromUser(id);

        // Delete user and return
        return userRepository.deleteById(id) > 0;
    }

    @Override
    @Transactional
    public boolean assignRoleToUser(Long userId, Integer roleId) {
        log.info("Assigning role {} to user {}", roleId, userId);

        // Check if user exists
        userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));

        // Check if role is already assigned
        if (roleRepository.countUserRoleAssociation(userId, roleId) > 0) {
            return false;
        }
        return roleRepository.addRoleToUser(userId, roleId) > 0;
    }

    @Override
    @Transactional
    public boolean removeRoleFromUser(Long userId, Integer roleId) {
        log.info("Removing role {} from user {}", roleId, userId);

        int result = userRepository.removeRoleFromUser(userId, roleId);
        log.info("Role removed successfully");
        return result > 0;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.debug("Loading user for authentication: {}", username);
        return userRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + username));
    }

    @Override
    @Transactional
    public void enableUser(String email) {
        log.info("Activating user account for email: {}", email);

        int rowsAffected = userRepository.enableUserByEmail(email);

        if (rowsAffected == 0) {
            throw new RuntimeException("User not found or already active: " + email);
        }
    }

    @Override
    @Transactional
    public void updatePassword(Long userId, String newPassword) {
        log.info("Updating password for user ID: {}", userId);

        // 1. Hash the password inside the service that owns the User entity
        String encodedPassword = passwordEncoder.encode(newPassword);

        // 2. Update DB
        int rows = userRepository.updatePassword(userId, encodedPassword);

        if (rows == 0) {
            throw new RuntimeException("Failed to update password. User not found.");
        }
    }
}