package com.example.buyfast.modules.company.service;

import com.example.buyfast.modules.company.dto.*;
import com.example.buyfast.modules.company.model.Company;
import com.example.buyfast.modules.user.model.User; // <-- NEW IMPORT
import jakarta.validation.Valid;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

public interface CompanyService {

    // --- MODIFIED ---
    
    CompanyDashboardDto getCompanyDashboard(UserDetails adminDetails);

    User createSeller(CreateSellerRequest request, UserDetails adminDetails);

    User updateSeller(UUID sellerUuid, UpdateSellerRequest request, UserDetails adminDetails);

    User deleteSeller(UUID sellerUuid, UserDetails adminDetails);

    Company updateCompanyProfile(UpdateCompanyRequest request, MultipartFile logoFile, UserDetails adminDetails);

    Company createCompany(@Valid CreateCompanyRequest request, MultipartFile logo, UserDetails userDetails);
}