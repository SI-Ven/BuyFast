package com.example.buyfast.modules.otp.model;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class SmsOtp {
    private Long id;
    private String phoneNumber;
    private String otpCode;
    private LocalDateTime expiresAt;
}