package com.example.buyfast.modules.auth.service;

import com.example.buyfast.modules.auth.dto.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

public interface AuthService {
    void register(RegisterRequest request);

    /**
     * Registers a new user as a Company Admin and creates their company.
     */
    void registerCompany(RegisterCompanyRequest request, MultipartFile logoFile); // <-- MODIFIED

    AuthResponse login(LoginRequest request);

    ResponseEntity<String> verifyOtp(OtpRequest request);

    void requestPasswordReset(ForgotPasswordRequest request);

    void resetPassword(ResetPasswordRequest request);
}