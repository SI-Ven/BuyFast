package com.example.buyfast.modules.company.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Company {
    private Long id;
    private UUID companyUuid;
    private String companyName;
    private String industryType;
    private String logoUrl;
    private String description;
    private boolean verified;
    private Long createdBy; // Internal ID of the admin_company user
    private LocalDateTime createdAt;
    private String status;
    private Double ratingAverage;
    private Integer maxSellers;
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