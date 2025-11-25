package com.example.buyfast.modules.company.controller;

import com.example.buyfast.common.ApiResponse;
import com.example.buyfast.modules.company.dto.CreateCompanyRequest;
import com.example.buyfast.modules.company.model.Company; // <-- NEW IMPORT
import com.example.buyfast.modules.company.service.CompanyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/company")
@RequiredArgsConstructor
public class CompanyController {

    private final CompanyService companyService;

    /**
     * Endpoint for an existing authenticated user (buyer/seller) to create a new company.
     * This will upgrade their role to 'admin_company'.
     */
    // --- MODIFIED ---
    @PostMapping
    public ResponseEntity<ApiResponse<Company>> createCompany( // <-- Changed to ApiResponse<Company>
                                                               @Valid @RequestBody CreateCompanyRequest request,
                                                               @AuthenticationPrincipal UserDetails userDetails) {

        Company company = companyService.createCompany(request, userDetails);
        return ResponseEntity.ok(ApiResponse.success("Company created successfully. You are now a company admin.", company));
    }

}