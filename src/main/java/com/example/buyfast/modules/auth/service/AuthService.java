package com.example.buyfast.modules.auth.service;

import com.example.buyfast.modules.auth.dto.*;
import com.example.buyfast.modules.user.model.User; // <-- NEW IMPORT
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

public interface AuthService {
    // --- MODIFIED ---
    User register(RegisterRequest request);

    /**
     * Registers a new user as a Company Admin and creates their company.
     */
    User registerCompany(RegisterCompanyRequest request, MultipartFile logoFile);

    AuthResponse login(LoginRequest request);

    /**
     * Verifies an OTP. Throws IllegalStateException if invalid.
     */
    User verifyOtp(OtpRequest request); // <-- MODIFIED

    // --- UNCHANGED ---
    void requestPasswordReset(ForgotPasswordRequest request);

    void resetPassword(ResetPasswordRequest request);
}