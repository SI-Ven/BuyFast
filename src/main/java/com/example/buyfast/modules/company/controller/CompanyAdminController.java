package com.example.buyfast.modules.company.controller;

import com.example.buyfast.common.ApiResponse;
import com.example.buyfast.modules.company.dto.CompanyDashboardDto;
import com.example.buyfast.modules.company.dto.CreateSellerRequest;
import com.example.buyfast.modules.company.dto.UpdateCompanyRequest;
import com.example.buyfast.modules.company.dto.UpdateSellerRequest;
import com.example.buyfast.modules.company.model.Company;
import com.example.buyfast.modules.company.service.CompanyService;
import com.example.buyfast.modules.user.model.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/company-admin")
@RequiredArgsConstructor
public class CompanyAdminController {

    private final CompanyService companyService;

    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<CompanyDashboardDto>> getDashboard(
            @AuthenticationPrincipal UserDetails userDetails) {
        CompanyDashboardDto dashboard = companyService.getCompanyDashboard(userDetails);
        return ResponseEntity.ok(ApiResponse.success("Dashboard retrieved successfully", dashboard));
    }

    @PostMapping("/seller")
    public ResponseEntity<ApiResponse<User>> createSeller(
            @Valid @RequestBody CreateSellerRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        User seller = companyService.createSeller(request, userDetails);
        return new ResponseEntity<>(
                ApiResponse.created("Seller created successfully.", seller),
                HttpStatus.CREATED
        );
    }

    @PutMapping("/seller/{sellerUuid}")
    public ResponseEntity<ApiResponse<User>> updateSeller(
            @PathVariable UUID sellerUuid,
            @Valid @RequestBody UpdateSellerRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        User seller = companyService.updateSeller(sellerUuid, request, userDetails);
        return ResponseEntity.ok(ApiResponse.success("Seller updated successfully.", seller));
    }

    @DeleteMapping("/seller/{sellerUuid}")
    public ResponseEntity<ApiResponse<User>> deleteSeller(
            @PathVariable UUID sellerUuid,
            @AuthenticationPrincipal UserDetails userDetails) {
        User seller = companyService.deleteSeller(sellerUuid, userDetails);
        return ResponseEntity.ok(ApiResponse.success("Seller deleted successfully.", seller));
    }

    // This controller method is also 100% correct
    @PutMapping(value = "/profile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<Company>> updateCompanyProfile(
            @Valid @RequestPart("request") UpdateCompanyRequest request,
            @RequestPart(value = "logoFile", required = false) MultipartFile logoFile, // 'required = false' is key
            @AuthenticationPrincipal UserDetails userDetails) {

        Company updatedCompany = companyService.updateCompanyProfile(request, logoFile, userDetails);
        return ResponseEntity.ok(ApiResponse.success("Company profile updated successfully.", updatedCompany));
    }
}