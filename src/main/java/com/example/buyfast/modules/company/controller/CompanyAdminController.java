package com.example.buyfast.modules.company.controller;

import com.example.buyfast.modules.company.dto.CompanyDashboardDto;
import com.example.buyfast.modules.company.dto.CreateSellerRequest;
import com.example.buyfast.modules.company.dto.UpdateCompanyRequest;
import com.example.buyfast.modules.company.dto.UpdateSellerRequest;
import com.example.buyfast.modules.company.service.CompanyService;
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
    public ResponseEntity<CompanyDashboardDto> getDashboard(
            @AuthenticationPrincipal UserDetails userDetails) {
        // ... (existing dashboard endpoint)
        CompanyDashboardDto dashboard = companyService.getCompanyDashboard(userDetails);
        return ResponseEntity.ok(dashboard);
    }

    @PostMapping("/seller")
    public ResponseEntity<Map<String, String>> createSeller(
            @Valid @RequestBody CreateSellerRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        // ... (existing create seller endpoint)
        companyService.createSeller(request, userDetails);
        return new ResponseEntity<>(Map.of("message", "Seller created successfully."), HttpStatus.CREATED);
    }

    @PutMapping("/seller/{sellerUuid}")
    public ResponseEntity<Map<String, String>> updateSeller(
            @PathVariable UUID sellerUuid,
            @Valid @RequestBody UpdateSellerRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        // ... (existing update seller endpoint)
        companyService.updateSeller(sellerUuid, request, userDetails);
        return ResponseEntity.ok(Map.of("message", "Seller updated successfully."));
    }

    @DeleteMapping("/seller/{sellerUuid}")
    public ResponseEntity<Map<String, String>> deleteSeller(
            @PathVariable UUID sellerUuid,
            @AuthenticationPrincipal UserDetails userDetails) {
        // ... (existing delete seller endpoint)
        companyService.deleteSeller(sellerUuid, userDetails);
        return ResponseEntity.ok(Map.of("message", "Seller deleted successfully."));
    }

    // --- THIS IS YOUR NEW ENDPOINT ---
    @PutMapping(value = "/profile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, String>> updateCompanyProfile(
            @Valid @RequestPart("request") UpdateCompanyRequest request,
            @RequestPart(value = "logoFile", required = false) MultipartFile logoFile,
            @AuthenticationPrincipal UserDetails userDetails) {

        companyService.updateCompanyProfile(request, logoFile, userDetails); // <-- Pass file
        return ResponseEntity.ok(Map.of("message", "Company profile updated successfully."));
    }
}