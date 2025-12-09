package com.example.buyfast.modules.auth.service;

import com.example.buyfast.modules.auth.dto.*;
import com.example.buyfast.modules.user.model.User;
import org.springframework.security.core.userdetails.UserDetails; // Import
import org.springframework.web.multipart.MultipartFile;

public interface AuthService {
    User register(RegisterRequest request);

    AuthResponse login(LoginRequest request);

    User verifyOtp(OtpRequest request);

    void requestPasswordReset(ForgotPasswordRequest request);

    void resetPassword(ResetPasswordRequest request);

    // --- NEW ---
    void logoutAll(UserDetails userDetails);
}