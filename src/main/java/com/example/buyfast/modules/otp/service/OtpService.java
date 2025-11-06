package com.example.buyfast.modules.otp.service;

public interface OtpService {
    /**
     * Generates, saves, and sends an OTP to the user's email.
     */
    void sendOtp(String email);

    /**
     * Verifies the OTP. If valid, it's deleted and returns true.
     */
    boolean verifyOtp(String email, String otpCode);
}