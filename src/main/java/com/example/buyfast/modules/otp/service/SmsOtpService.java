package com.example.buyfast.modules.otp.service;

public interface SmsOtpService {
    void sendOtp(String phoneNumber);
    boolean verifyOtp(String phoneNumber, String otpCode);
}