package com.example.buyfast.modules.admin.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateProductStatusRequest {

    // We use the Boolean object so @NotNull can check if it's missing
    @NotNull(message = "isActive is required (true or false)")
    private Boolean isActive;
}