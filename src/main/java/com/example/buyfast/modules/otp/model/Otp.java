package com.example.buyfast.modules.otp.model;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Otp {
    private Long id;
    private String email;
    private String otpCode;
    private LocalDateTime expiresAt;
}