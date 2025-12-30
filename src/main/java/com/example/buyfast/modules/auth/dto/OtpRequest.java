package com.example.buyfast.modules.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class OtpRequest {
    @NotBlank
    @Email
    private String email;

    @NotBlank
    private String otpCode;
}