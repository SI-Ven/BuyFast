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

    // --- NEW FIELD ---
    @NotBlank(message = "Tax ID is required")
    @Size(max = 50)
    private String taxId;

    private String description;

    @NotBlank(message = "Phone number is required")
    @Size(max = 20)
    private String phoneNumber;

    // --- ADDRESS FIELDS ---
    @NotBlank(message = "Address line 1 is required")
    @Size(max = 255)
    private String addressLine1;

    @NotBlank(message = "City is required")
    @Size(max = 100)
    private String city;

    @Size(max = 100)
    private String stateProvince;

    @NotBlank(message = "Postal code is required")
    @Size(max = 20)
    private String postalCode;

    @NotBlank(message = "Country is required")
    @Size(max = 100)
    private String country;
}