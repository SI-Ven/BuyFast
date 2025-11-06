package com.example.buyfast.modules.company.dto;

import com.example.buyfast.modules.company.model.Company;
import lombok.Data;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
public class CompanyDashboardDto {
    private UUID companyUuid;
    private String companyName;
    private String industryType;
    private String logoUrl;
    private String description;
    private boolean isVerified;
    private int currentSellerCount;
    private int maxSellers;
    private List<SellerProfileDto> sellers;
    private Map<String, Double> sellerProfits; // Placeholder for profit summary

    public static CompanyDashboardDto fromCompany(Company company, List<SellerProfileDto> sellers, int sellerCount) {
        CompanyDashboardDto dto = new CompanyDashboardDto();
        dto.setCompanyUuid(company.getCompanyUuid());
        dto.setCompanyName(company.getCompanyName());
        dto.setIndustryType(company.getIndustryType());
        dto.setLogoUrl(company.getLogoUrl());
        dto.setDescription(company.getDescription());
        dto.setVerified(company.isVerified());
        dto.setMaxSellers(company.getMaxSellers());
        dto.setSellers(sellers);
        dto.setCurrentSellerCount(sellerCount);

        // TODO: Implement profit logic by querying orders/products
        dto.setSellerProfits(Map.of("totalProfit", 0.00)); // Placeholder

        return dto;
    }
}