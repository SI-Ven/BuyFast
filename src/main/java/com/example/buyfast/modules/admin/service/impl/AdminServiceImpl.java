package com.example.buyfast.modules.admin.service.Impl;

import com.example.buyfast.modules.admin.service.AdminService;
import com.example.buyfast.modules.company.model.Company;
import com.example.buyfast.modules.company.repository.CompanyRepo;
import com.example.buyfast.modules.product.model.Product;
import com.example.buyfast.modules.product.repository.ProductRepo;
import com.example.buyfast.modules.user.model.User;
import com.example.buyfast.modules.user.repository.UserRepo;
import com.example.buyfast.modules.verify.model.Verify;
import com.example.buyfast.modules.verify.repository.VerifyRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final UserRepo userRepo;
    private final CompanyRepo companyRepo;
    private final ProductRepo productRepo;
    private final VerifyRepo verifyRepo;

    @Override
    public List<User> getAllUsers() {
        return userRepo.findAllUsers();
    }

    @Override
    public List<Company> getAllCompanies() {
        return companyRepo.findAllCompanies();
    }

    @Override
    public List<Product> getAllProducts() {
        return productRepo.findAllProducts();
    }

    @Override
    public List<Verify> getAllVerificationRequests() {
        return verifyRepo.findAllVerificationRequests();
    }

    @Override
    public List<Verify> getPendingVerificationRequests() {
        return verifyRepo.findAllPendingVerificationRequests();
    }

    @Override
    @Transactional
    public User updateUserStatus(UUID userUuid, String status) {
        // Find user to ensure they exist
        User user = userRepo.findByUuid(userUuid)
                .orElseThrow(() -> new IllegalStateException("User not found with UUID: " + userUuid));

        // Update their status
        userRepo.updateUserStatusByUuid(userUuid, status);
        user.setStatus(status);
        return user;
    }

    @Override
    @Transactional
    public Company updateCompanyStatus(UUID companyUuid, String status) {
        // Find company to ensure it exists
        Company company = companyRepo.findByUuid(companyUuid)
                .orElseThrow(() -> new IllegalStateException("Company not found with UUID: " + companyUuid));

        // Use the existing method to update status by internal ID
        companyRepo.updateCompanyStatus(company.getId(), status);
        company.setStatus(status);
        return company;
    }

    @Override
    @Transactional
    public Product updateProductStatus(UUID productUuid, boolean isActive) {
        // Find product to ensure it exists
        Product product = productRepo.findByUuid(productUuid)
                .orElseThrow(() -> new IllegalStateException("Product not found with UUID: " + productUuid));

        // Update its active status
        productRepo.updateProductActiveStatus(productUuid, isActive);
        product.setActive(isActive);
        return product;
    }
}