package com.example.buyfast.modules.admin.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateStatusRequest {

    @NotBlank(message = "Status is required (e.g., 'active', 'banned', 'suspended')")
    private String status;
}