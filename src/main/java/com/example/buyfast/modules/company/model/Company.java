package com.example.buyfast.modules.company.model;

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
}