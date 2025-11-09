package com.example.buyfast.modules.admin.controller;

import com.example.buyfast.common.ApiResponse;
// --- NEW IMPORTS ---
import com.example.buyfast.modules.admin.dto.UpdateProductStatusRequest;
import com.example.buyfast.modules.admin.dto.UpdateStatusRequest;
import com.example.buyfast.modules.admin.service.AdminService;
import com.example.buyfast.modules.company.model.Company;
import com.example.buyfast.modules.product.model.Product;
import com.example.buyfast.modules.user.model.User;
import com.example.buyfast.modules.verify.model.Verify;
import jakarta.validation.Valid; // --- NEW IMPORT ---
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
// import java.util.Map; // --- NO LONGER NEEDED ---
import java.util.UUID;

/**
 * Controller for Super Admin (admin_platform) to manage the platform.
 */
@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    // --- GET ALL (for dashboards) ---
    // ... (getAllUsers, getAllCompanies, etc. are unchanged) ...
    @GetMapping("/users")
    public ResponseEntity<ApiResponse<List<User>>> getAllUsers() {
        List<User> users = adminService.getAllUsers();
        return ResponseEntity.ok(ApiResponse.success("All users retrieved", users));
    }

    @GetMapping("/companies")
    public ResponseEntity<ApiResponse<List<Company>>> getAllCompanies() {
        List<Company> companies = adminService.getAllCompanies();
        return ResponseEntity.ok(ApiResponse.success("All companies retrieved", companies));
    }

    @GetMapping("/products")
    public ResponseEntity<ApiResponse<List<Product>>> getAllProducts() {
        List<Product> products = adminService.getAllProducts();
        return ResponseEntity.ok(ApiResponse.success("All products retrieved", products));
    }

    @GetMapping("/verifications")
    public ResponseEntity<ApiResponse<List<Verify>>> getAllVerificationRequests() {
        List<Verify> requests = adminService.getAllVerificationRequests();
        return ResponseEntity.ok(ApiResponse.success("All verification requests retrieved", requests));
    }

    @GetMapping("/verifications/pending")
    public ResponseEntity<ApiResponse<List<Verify>>> getPendingVerificationRequests() {
        List<Verify> requests = adminService.getPendingVerificationRequests();
        return ResponseEntity.ok(ApiResponse.success("Pending verification requests retrieved", requests));
    }


    // --- MANAGE STATUS (for moderation) ---

    /**
     * Update a user's status (e.g., "active", "banned")
     * @param request {"status": "banned"}
     */
    // --- MODIFIED ---
    @PostMapping("/users/{userUuid}/status")
    public ResponseEntity<ApiResponse<User>> updateUserStatus(
            @PathVariable UUID userUuid,
            @Valid @RequestBody UpdateStatusRequest request) { // <-- Use DTO

        // Get status directly from the validated request object
        String status = request.getStatus();

        User user = adminService.updateUserStatus(userUuid, status);
        return ResponseEntity.ok(ApiResponse.success("User status updated to '" + status + "'", user));
    }

    /**
     * Update a company's status (e.g., "active", "suspended")
     * @param request {"status": "suspended"}
     */
    // --- MODIFIED ---
    @PostMapping("/companies/{companyUuid}/status")
    public ResponseEntity<ApiResponse<Company>> updateCompanyStatus(
            @PathVariable UUID companyUuid,
            @Valid @RequestBody UpdateStatusRequest request) { // <-- Use DTO

        // Get status directly from the validated request object
        String status = request.getStatus();

        Company company = adminService.updateCompanyStatus(companyUuid, status);
        return ResponseEntity.ok(ApiResponse.success("Company status updated to '" + status + "'", company));
    }

    /**
     * Update a product's active status (e.g., true, false)
     * @param request {"isActive": false}
     */
    // --- MODIFIED ---
    @PostMapping("/products/{productUuid}/status")
    public ResponseEntity<ApiResponse<Product>> updateProductStatus(
            @PathVariable UUID productUuid,
            @Valid @RequestBody UpdateProductStatusRequest request) { // <-- Use DTO

        // Get status directly from the validated request object
        Boolean isActive = request.getIsActive();

        Product product = adminService.updateProductStatus(productUuid, isActive);
        return ResponseEntity.ok(ApiResponse.success("Product active status updated to " + isActive, product));
    }
}