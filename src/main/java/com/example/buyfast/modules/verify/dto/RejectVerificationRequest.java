package com.example.buyfast.modules.verify.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RejectVerificationRequest {
    @NotBlank(message = "Rejection remarks are required.")
    private String remarks;
}