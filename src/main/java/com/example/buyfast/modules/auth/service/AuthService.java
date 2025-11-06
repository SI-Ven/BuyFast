package com.example.buyfast.modules.auth.service;

import com.example.buyfast.modules.auth.dto.*;

public interface AuthService {
    /**
     * Registers a new user, sets their status to 'pending', and triggers an OTP.
     */
    void register(RegisterRequest request);

    /**
     * Attempts to log in a user. Fails if credentials are bad or account is not 'active'.
     */
    AuthResponse login(LoginRequest request);

    /**
     * Verifies an OTP, activates the user, and returns a login token.
     */
    AuthResponse verifyOtp(OtpRequest request);

    /**
     * Triggers an OTP send for an existing, active user.
     */
    void requestPasswordReset(ForgotPasswordRequest request);

    /**
     * Verifies the OTP and updates the user's password.
     */
    void resetPassword(ResetPasswordRequest request);
}