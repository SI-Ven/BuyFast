package com.example.buyfast.modules.company.controller;

import com.example.buyfast.common.ApiResponse;
import com.example.buyfast.modules.company.dto.CreateCompanyRequest;
import com.example.buyfast.modules.company.model.Company; // <-- NEW IMPORT
import com.example.buyfast.modules.company.service.CompanyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<Company>> createCompany(
            @RequestPart("request") @Valid CreateCompanyRequest request,
            @RequestPart(value = "logo", required = false) MultipartFile logo,
            @AuthenticationPrincipal UserDetails userDetails) {

        Company company = companyService.createCompany(request, logo, userDetails);

        return ResponseEntity.ok(ApiResponse.success(
                "Company created successfully. You are now a supplier.",
                company
        ));
    }

}