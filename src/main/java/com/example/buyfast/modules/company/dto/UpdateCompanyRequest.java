package com.example.buyfast.modules.company.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

// DTO for allowing an admin to update their company profile
@Data
public class UpdateCompanyRequest {

    // Only include fields they are allowed to change
    // We don't let them change company name or industry type easily
    // but they can update description, logo, and address.

    @Size(min = 10, max = 2000)
    private String description;


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