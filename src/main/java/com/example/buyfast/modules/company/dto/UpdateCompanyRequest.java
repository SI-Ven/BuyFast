package com.example.buyfast.modules.company.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateCompanyRequest {
    @Size(min = 3, max = 255) // <-- Add validation for the name
    private String companyName; // <-- ADD THIS LINE

    @Size(min = 10, max = 2000)
    private String description;

    // logoUrl is removed from here

    @Size(max = 255)
    private String addressLine1;

    @Size(max = 100)
    private String city;

    @Size(max = 100)
    private String stateProvince;

    @Size(max = 20)
    private String postalCode;

    @Size(max = 100)
    private String country;
}