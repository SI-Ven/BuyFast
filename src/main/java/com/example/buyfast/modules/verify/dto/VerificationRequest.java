package com.example.buyfast.modules.verify.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class VerificationRequest {

    @NotBlank(message = "ID Card URL is required")
    private String idCardUrl;
    // In a real app, this would be a file upload,
    // but for now, we assume a separate service has uploaded it and provided this URL.
}