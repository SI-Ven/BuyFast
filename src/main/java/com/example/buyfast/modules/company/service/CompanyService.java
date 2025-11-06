package com.example.buyfast.modules.company.service;

import com.example.buyfast.modules.company.dto.*;
import org.springframework.security.core.userdetails.UserDetails;
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
}