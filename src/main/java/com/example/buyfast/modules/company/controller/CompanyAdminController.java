package com.example.buyfast.modules.company.controller;

import com.example.buyfast.modules.company.dto.CompanyDashboardDto;
import com.example.buyfast.modules.company.dto.CreateSellerRequest;
import com.example.buyfast.modules.company.dto.UpdateSellerRequest;
import com.example.buyfast.modules.company.service.CompanyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/company-admin")
@RequiredArgsConstructor
public class CompanyAdminController {

    private final CompanyService companyService;

    /**
     * Gets the dashboard for the company admin.
     */
    @GetMapping("/dashboard")
    public ResponseEntity<CompanyDashboardDto> getDashboard(
            @AuthenticationPrincipal UserDetails userDetails) {

        CompanyDashboardDto dashboard = companyService.getCompanyDashboard(userDetails);
        return ResponseEntity.ok(dashboard);
    }

    /**
     * Creates a new seller under the admin's company.
     */
    @PostMapping("/seller")
    public ResponseEntity<Map<String, String>> createSeller(
            @Valid @RequestBody CreateSellerRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        companyService.createSeller(request, userDetails);
        return new ResponseEntity<>(Map.of("message", "Seller created successfully."), HttpStatus.CREATED);
    }

    /**
     * Updates an existing seller under the admin's company.
     */
    @PutMapping("/seller/{sellerUuid}")
    public ResponseEntity<Map<String, String>> updateSeller(
            @PathVariable UUID sellerUuid,
            @Valid @RequestBody UpdateSellerRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        companyService.updateSeller(sellerUuid, request, userDetails);
        return ResponseEntity.ok(Map.of("message", "Seller updated successfully."));
    }

    /**
     * Deletes a seller from the admin's company.
     */
    @DeleteMapping("/seller/{sellerUuid}")
    public ResponseEntity<Map<String, String>> deleteSeller(
            @PathVariable UUID sellerUuid,
            @AuthenticationPrincipal UserDetails userDetails) {

        companyService.deleteSeller(sellerUuid, userDetails);
        return ResponseEntity.ok(Map.of("message", "Seller deleted successfully."));
    }
}