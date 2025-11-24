package com.example.buyfast.modules.auth.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Otp {
    private Long id;
    private String email;
    private String otpCode;
    private Boolean isUsed;
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;
}
