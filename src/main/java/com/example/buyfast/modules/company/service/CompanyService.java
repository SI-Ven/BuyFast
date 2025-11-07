package com.example.buyfast.modules.company.service;

import com.example.buyfast.modules.company.dto.*;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.multipart.MultipartFile; // <-- ADDED

import java.util.UUID;

public interface CompanyService {

    void createCompany(CreateCompanyRequest request, UserDetails adminDetails);

    CompanyDashboardDto getCompanyDashboard(UserDetails adminDetails);

    void createSeller(CreateSellerRequest request, UserDetails adminDetails);

    void updateSeller(UUID sellerUuid, UpdateSellerRequest request, UserDetails adminDetails);

    void deleteSeller(UUID sellerUuid, UserDetails adminDetails);

    /**
     * Updates the company profile for the currently logged-in admin.
     */
    void updateCompanyProfile(UpdateCompanyRequest request, MultipartFile logoFile, UserDetails adminDetails); // <-- MODIFIED
}