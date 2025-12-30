package com.example.buyfast.modules.otp.service;

public interface OtpService {

    void sendOtp(String email);


    boolean verifyOtp(String email, String otpCode);
}