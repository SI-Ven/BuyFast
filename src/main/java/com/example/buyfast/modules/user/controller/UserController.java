package com.example.buyfast.modules.user.controller;

import com.example.buyfast.common.dto.ApiResponse;
import com.example.buyfast.modules.user.dto.UserCreateRequest;
import com.example.buyfast.modules.user.dto.UserResponse;
import com.example.buyfast.modules.user.service.UserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Validated
public class UserController {

    private final UserService userService;

    // 1. Create User
    // Permission: "user:create" (Only Admins usually have this)
    @PostMapping
    @PreAuthorize("hasAuthority('user:create')")
    public ResponseEntity<ApiResponse<UserResponse>> createUser(@Valid @RequestBody UserCreateRequest request) {
        UserResponse createdUser = userService.createUser(request);
        return buildResponse(createdUser, "User created successfully", HttpStatus.CREATED);
    }

    // 2. Get User By ID
    // Permission: "user:read" (Admins, Support, and the User themselves might have this)
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('user:read')")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(@PathVariable Long id) {
        log.info("REST request to get user by ID: {}", id);
        return buildResponse(userService.getUserById(id), "User found", HttpStatus.OK);
    }

    // 3. Get User By Public ID
    // Permission: "user:read"
    @GetMapping("/public/{publicId}")
    @PreAuthorize("hasAuthority('user:read')")
    public ResponseEntity<ApiResponse<UserResponse>> getUserByPublicId(@PathVariable UUID publicId) {
        log.info("REST request to get user by public ID: {}", publicId);
        return buildResponse(userService.getUserByPublicId(publicId), "User found", HttpStatus.OK);
    }

    // 4. Get User By Username
    // Permission: "user:read"
    @GetMapping("/username/{username}")
    @PreAuthorize("hasAuthority('user:read')")
    public ResponseEntity<ApiResponse<UserResponse>> getUserByUsername(@PathVariable String username) {
        log.info("REST request to get user by username: {}", username);
        return buildResponse(userService.getUserByUsername(username), "User found", HttpStatus.OK);
    }

    // 5. Get User By Email (Sensitive!)
    // Permission: "user:read-email" (Maybe only Super Admins should have this specific permission)
    @GetMapping("/email/{email}")
    @PreAuthorize("hasAuthority('user:read-email')")
    public ResponseEntity<ApiResponse<UserResponse>> getUserByEmail(@PathVariable @Email String email) {
        log.info("REST request to get user by email: {}", email);
        return buildResponse(userService.getUserByEmail(email), "User found", HttpStatus.OK);
    }

    // 6. List All Users
    // Permission: "user:list" (Different from reading a single user)
    @GetMapping
    @PreAuthorize("hasAuthority('user:list')")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getAllUsers() {
        log.info("REST request to get all users");
        List<UserResponse> users = userService.getAllUsers();
        return buildResponse(users, "Found " + users.size() + " users", HttpStatus.OK);
    }

    // 7. List Active Users
    // Permission: "user:list"
    @GetMapping("/active")
    @PreAuthorize("hasAuthority('user:list')")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getUsersByActiveStatus(@RequestParam Boolean status) {
        log.info("REST request to get users by active status: {}", status);
        List<UserResponse> users = userService.getUsersByActiveStatus(status);
        return buildResponse(users, "Found " + users.size() + " users with status " + status, HttpStatus.OK);
    }

    // --- Helper to reduce duplicated code ---
    private <T> ResponseEntity<ApiResponse<T>> buildResponse(T payload, String message, HttpStatus status) {
        ApiResponse<T> response = ApiResponse.<T>builder()
                .message(message)
                .payload(payload)
                .status(status.value())
                .timestamp(LocalDateTime.now())
                .build();
        return new ResponseEntity<>(response, status);
    }
}