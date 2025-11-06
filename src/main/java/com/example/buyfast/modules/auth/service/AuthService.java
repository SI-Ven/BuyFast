package com.example.buyfast.modules.auth.service;

import com.example.buyfast.modules.auth.dto.AuthResponse;
import com.example.buyfast.modules.auth.dto.LoginRequest;
import com.example.buyfast.modules.auth.dto.OtpRequest;
import com.example.buyfast.modules.auth.dto.RegisterRequest;

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
}