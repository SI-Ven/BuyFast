package com.example.buyfast.modules.auth.controller;

import com.example.buyfast.modules.auth.dto.*;
import com.example.buyfast.modules.auth.service.AuthService;
import com.example.buyfast.modules.otp.service.OtpService;
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

    @PostMapping("/register")
    public ResponseEntity<Map<String, String>> register(
            @Valid @RequestBody RegisterRequest request
    ) {
        authService.register(request);
        return ResponseEntity.ok(Map.of("message", "User registered successfully. Please check your email for the OTP."));
    }

    // --- NEW ENDPOINT ---
    /**
     * Registers a new user as a Company Admin and creates their company
     * in a single step.
     */
    @PostMapping(value = "/register-company", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, String>> registerCompany(
            @Valid @RequestPart("request") RegisterCompanyRequest request,
            @RequestPart(value = "logoFile", required = false) MultipartFile logoFile
    ) {
        authService.registerCompany(request, logoFile); // <-- Pass the file to the service
        return ResponseEntity.ok(Map.of("message", "Company and admin user registered successfully. Please check your email for the OTP."));
    }
    // --- END OF NEW ENDPOINT ---

    @PostMapping("/verify-otp")
    public ResponseEntity<String> verifyOtp(
            @Valid @RequestBody OtpRequest request
    ) {
        // This now returns the ResponseEntity<String> from the service
        return authService.verifyOtp(request);
    }

    @PostMapping("/resend-otp")
    public ResponseEntity<Map<String, String>> resendOtp(
            @Valid @RequestBody ResendOtpRequest request
    ) {
        otpService.sendOtp(request.getEmail());
        return ResponseEntity.ok(Map.of("message", "OTP has been resent."));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody LoginRequest request
    ) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, String>> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request
    ) {
        authService.requestPasswordReset(request);
        return ResponseEntity.ok(Map.of("message", "If an active account exists for this email, an OTP has been sent."));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, String>> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request
    ) {
        authService.resetPassword(request);
        return ResponseEntity.ok(Map.of("message", "Password has been reset successfully. You can now login."));
    }


    // --- EXCEPTION HANDLERS (Unchanged) ---

    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<Map<String, String>> handleDisabledException(DisabledException ex) {
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("error", "Account is not verified. Please check your email for the OTP."));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, String>> handleIllegalStateException(IllegalStateException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", ex.getMessage()));
    }
}