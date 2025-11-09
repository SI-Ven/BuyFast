package com.example.buyfast.modules.admin.service;

import com.example.buyfast.modules.company.model.Company;
import com.example.buyfast.modules.product.model.Product;
import com.example.buyfast.modules.user.model.User;
import com.example.buyfast.modules.verify.model.Verify;

import java.util.List;
import java.util.UUID;

/**
 * Service layer for Super Admin (admin_platform) functionalities.
 */
public interface AdminService {

    // --- Data Retrieval ---
    List<User> getAllUsers();
    List<Company> getAllCompanies();
    List<Product> getAllProducts();
    List<Verify> getAllVerificationRequests();
    List<Verify> getPendingVerificationRequests();

    // --- Management Actions ---
    User updateUserStatus(UUID userUuid, String status);
    Company updateCompanyStatus(UUID companyUuid, String status);
    Product updateProductStatus(UUID productUuid, boolean isActive);
}