package com.example.buyfast.modules.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterCompanyRequest {

    // User Fields
    @NotBlank
    @Size(min = 2, max = 100)
    private String firstName;

    @NotBlank
    @Size(min = 2, max = 100)
    private String lastName;

    @NotBlank
    @Email
    private String email;

    @NotBlank
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;

    // Company Fields
    @NotBlank(message = "Company name is required")
    @Size(min = 3, max = 255)
    private String companyName;

    @NotBlank(message = "Industry type is required")
    @Size(min = 3, max = 100)
    private String industryType;

    private String description;



    // --- NEW ADDRESS FIELDS ---
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
    // --- END NEW FIELDS ---
}