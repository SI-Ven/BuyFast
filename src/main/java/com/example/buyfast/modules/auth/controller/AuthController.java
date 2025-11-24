package com.example.buyfast.modules.auth.controller;

import com.example.buyfast.common.dto.ApiResponse;
import com.example.buyfast.modules.auth.dto.*;
import com.example.buyfast.modules.auth.service.AuthService;
import com.example.buyfast.modules.user.dto.RegisterRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    // 1. Register Buyer (No Token Returned)
    @PostMapping("/register/buyer")
    public ResponseEntity<ApiResponse<Void>> registerBuyer(@Valid @RequestBody RegisterRequest request) {
        authService.registerBuyer(request);
        return buildResponse("OTP sent to email. Please verify to complete registration.", HttpStatus.CREATED);
    }

    // 2. Register Supplier (No Token Returned)
    @PostMapping("/register/supplier")
    public ResponseEntity<ApiResponse<Void>> registerSupplier(@Valid @RequestBody RegisterRequest request) {
        authService.registerSupplier(request);
        return buildResponse("OTP sent to email. Please verify to complete registration.", HttpStatus.CREATED);
    }

    // 3. Verify OTP (Returns Token)
    @PostMapping("/verify-otp")
    public ResponseEntity<ApiResponse<AuthResponse>> verifyOtp(@Valid @RequestBody VerifyOtpRequest request) {
        AuthResponse authResponse = authService.verifyOtp(request);
        return buildResponse(authResponse, "Verification successful. Logged in.", HttpStatus.OK);
    }

    // 4. Login (Standard)
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse authResponse = authService.login(request);
        return buildResponse(authResponse, "Login successful", HttpStatus.OK);
    }

    // 5. Resend OTP
    @PostMapping("/resend-otp")
    public ResponseEntity<ApiResponse<Void>> resendOtp(@Valid @RequestBody ResendOtpRequest request) {
        authService.resendOtp(request);
        return buildResponse("OTP sent successfully.", HttpStatus.OK);
    }

    // 6. Forgot Password
    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        authService.forgotPassword(request);
        // Security Best Practice: Always say "If account exists..." to prevent email enumeration attacks
        return buildResponse("If an account exists with this email, an OTP has been sent.", HttpStatus.OK);
    }

    // 7. Reset Password
    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<Void>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request);
        return buildResponse("Password reset successfully. You can now login.", HttpStatus.OK);
    }

    // --- Helper Methods to clean up code ---

    // Helper for Void responses (Success messages only)
    private ResponseEntity<ApiResponse<Void>> buildResponse(String message, HttpStatus status) {
        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .message(message)
                .status(status.value())
                .timestamp(LocalDateTime.now())
                .build();
        return new ResponseEntity<>(response, status);
    }

    // Helper for Data responses (AuthResponse)
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