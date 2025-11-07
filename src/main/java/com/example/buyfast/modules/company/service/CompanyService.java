package com.example.buyfast.modules.company.service;

import com.example.buyfast.modules.company.dto.*;
import com.example.buyfast.modules.company.model.Company;
import jakarta.validation.Valid;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

public interface CompanyService {

    /**
     * Creates a new company, making the calling user the 'admin_company'.
     */
    void createCompany(CreateCompanyRequest request, UserDetails adminDetails);

    /**
     * Gets the dashboard for the currently logged-in company admin.
     */
    CompanyDashboardDto getCompanyDashboard(UserDetails adminDetails);

    /**
     * Creates a new seller under the admin's company.
     */
    void createSeller(CreateSellerRequest request, UserDetails adminDetails);

    /**
     * Updates a seller managed by the admin.
     */
    void updateSeller(UUID sellerUuid, UpdateSellerRequest request, UserDetails adminDetails);

    /**
     * Deletes a seller managed by the admin.
     */
    void deleteSeller(UUID sellerUuid, UserDetails adminDetails);

    void updateCompanyProfile(UpdateCompanyRequest request, MultipartFile logoFile, UserDetails adminDetails); // <-- MODIFIED
}