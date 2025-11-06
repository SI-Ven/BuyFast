package com.example.buyfast.modules.company.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateSellerRequest {

    @Size(min = 2, max = 100)
    private String firstName;

    @Size(min = 2, max = 100)
    private String lastName;

    @NotBlank(message = "Status is required (e.g., 'active', 'banned')")
    private String status;
}