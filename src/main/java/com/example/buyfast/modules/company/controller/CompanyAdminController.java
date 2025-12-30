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

import java.util.List;
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

    @PutMapping(value = "/profile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<Company>> updateCompanyProfile(
            @RequestParam(value = "companyName", required = false) String companyName,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "addressLine1", required = false) String addressLine1,
            @RequestParam(value = "city", required = false) String city,
            @RequestParam(value = "stateProvince", required = false) String stateProvince,
            @RequestParam(value = "postalCode", required = false) String postalCode,
            @RequestParam(value = "country", required = false) String country,
            @RequestPart(value = "logoFile", required = false) MultipartFile logoFile,
            @AuthenticationPrincipal UserDetails userDetails) {

        // Manually create the DTO from the params to handle Multipart/form-data correctly
        UpdateCompanyRequest request = new UpdateCompanyRequest();
        request.setCompanyName(companyName);
        request.setDescription(description);
        request.setAddressLine1(addressLine1);
        request.setCity(city);
        request.setStateProvince(stateProvince);
        request.setPostalCode(postalCode);
        request.setCountry(country);

        Company updatedCompany = companyService.updateCompanyProfile(request, logoFile, userDetails);

        return ResponseEntity.ok(ApiResponse.success("Company profile updated successfully.", updatedCompany));
    }

    @GetMapping("/sellers")
    public ResponseEntity<ApiResponse<List<User>>> getCompanySellers(
            @AuthenticationPrincipal UserDetails userDetails) {
        // You need to implement getSellers in CompanyService
        // Assuming CompanyService has a method to find users by CompanyId and Role 'seller'
        List<User> sellers = companyService.getCompanySellers(userDetails);
        return ResponseEntity.ok(ApiResponse.success("Sellers retrieved successfully", sellers));
    }
}