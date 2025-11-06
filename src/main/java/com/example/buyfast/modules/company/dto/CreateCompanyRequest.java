package com.example.buyfast.modules.company.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateCompanyRequest {

    @NotBlank(message = "Company name is required")
    @Size(min = 3, max = 255)
    private String companyName;

    @NotBlank(message = "Industry type is required")
    @Size(min = 3, max = 100)
    private String industryType;

    private String description;

    @Size(max = 512)
    private String logoUrl;
}