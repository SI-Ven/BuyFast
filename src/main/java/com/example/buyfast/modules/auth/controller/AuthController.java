package com.example.buyfast.modules.auth.controller;

import com.example.buyfast.common.ApiResponse;
import com.example.buyfast.modules.auth.dto.*;
import com.example.buyfast.modules.auth.service.AuthService;
import com.example.buyfast.modules.otp.service.OtpService;
import com.example.buyfast.modules.user.model.User; // <-- NEW IMPORT
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.DisabledException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final OtpService otpService;

    // --- MODIFIED ---
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<User>> register( // <-- Changed to ApiResponse<User>
                                                       @Valid @RequestBody RegisterRequest request
    ) {
        User user = authService.register(request);
        return ResponseEntity.ok(ApiResponse.success("User registered successfully. Please check your email for the OTP.", user));
    }

    // --- MODIFIED ---
    @PostMapping(value = "/register-company", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<User>> registerCompany( // <-- Changed to ApiResponse<User>
                                                              @Valid @RequestPart("request") RegisterCompanyRequest request,
                                                              @RequestPart(value = "logoFile", required = false) MultipartFile logoFile
    ) {
        User user = authService.registerCompany(request, logoFile);
        return ResponseEntity.ok(ApiResponse.success("Company and admin user registered successfully. Please check your email for the OTP.", user));
    }

    // --- MODIFIED ---
    @PostMapping("/verify-otp")
    public ResponseEntity<ApiResponse<User>> verifyOtp( // <-- Changed to ApiResponse<User>
                                                        @Valid @RequestBody OtpRequest request
    ) {
        User user = authService.verifyOtp(request);
        return ResponseEntity.ok(ApiResponse.success("Otp verified successfully", user));
    }

    // --- UNCHANGED (resend-otp, login, etc.) ---
    @PostMapping("/resend-otp")
    public ResponseEntity<ApiResponse<Object>> resendOtp(
            @Valid @RequestBody ResendOtpRequest request
    ) {
        otpService.sendOtp(request.getEmail());
        return ResponseEntity.ok(ApiResponse.ok("OTP has been resent."));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request
    ) {
        AuthResponse authResponse = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success("Login successful", authResponse));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<Object>> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request
    ) {
        authService.requestPasswordReset(request);
        return ResponseEntity.ok(ApiResponse.ok("If an active account exists for this email, an OTP has been sent."));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<Object>> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request
    ) {
        authService.resetPassword(request);
        return ResponseEntity.ok(ApiResponse.ok("Password has been reset successfully. You can now login."));
    }

    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<ApiResponse<Object>> handleDisabledException(DisabledException ex) {
        ApiResponse<Object> response = ApiResponse.error(HttpStatus.UNAUTHORIZED, "Account is not verified. Please check your email for the OTP.");
        return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiResponse<Object>> handleIllegalStateException(IllegalStateException ex) {
        ApiResponse<Object> response = ApiResponse.error(HttpStatus.BAD_REQUEST, ex.getMessage());
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }
}