package com.example.buyfast.modules.company.service.Impl;

import com.example.buyfast.modules.company.dto.*;
import com.example.buyfast.modules.company.model.Company;
import com.example.buyfast.modules.company.repository.CompanyRepo;
import com.example.buyfast.modules.company.service.CompanyService;
import com.example.buyfast.modules.storage.service.StorageService;
import com.example.buyfast.modules.user.model.User;
import com.example.buyfast.modules.user.repository.UserRepo;
import com.example.buyfast.util.UuidService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile; // <-- ADDED

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CompanyServiceImpl implements CompanyService {

    private final CompanyRepo companyRepo;
    private final UserRepo userRepo;
    private final UuidService uuidService;
    private final PasswordEncoder passwordEncoder;
    private final StorageService storageService; // <-- Injected

    @Override
    @Transactional
    public void createCompany(CreateCompanyRequest request, UserDetails adminDetails) {
        // ... (existing createCompany logic)
        User adminUser = (User) adminDetails;

        if (adminUser.getCompanyId() != null) {
            throw new IllegalStateException("User is already part of a company.");
        }
        if (!"buyer".equals(adminUser.getRole()) && !"seller".equals(adminUser.getRole())) {
            throw new IllegalStateException("Only buyers or individual sellers can create a new company.");
        }
        if (companyRepo.findByAdminId(adminUser.getId()).isPresent()) {
            throw new IllegalStateException("User already owns a company.");
        }

        Company company = new Company();
        company.setCompanyUuid(uuidService.generateUuid());
        company.setCompanyName(request.getCompanyName());
        company.setIndustryType(request.getIndustryType());
        company.setLogoUrl(request.getLogoUrl()); // This is null, will be updated by registerCompany
        company.setDescription(request.getDescription());
        company.setCreatedBy(adminUser.getId());
        company.setMaxSellers(3);
        company.setStatus("active");

        // Note: Address fields are NOT set here, they are set in registerCompany flow

        companyRepo.insert(company); // This insert is for the old flow, might need adjustment

        userRepo.updateUserRoleAndCompany(adminUser.getId(), "admin_company", company.getId());
    }

    @Override
    public CompanyDashboardDto getCompanyDashboard(UserDetails adminDetails) {
        // ... (existing getCompanyDashboard logic)
        User adminUser = (User) adminDetails;
        Company company = companyRepo.findById(adminUser.getCompanyId())
                .orElseThrow(() -> new IllegalStateException("Admin is not associated with a valid company."));
        List<User> sellers = userRepo.findSellersByCompanyId(company.getId());
        List<SellerProfileDto> sellerDtos = sellers.stream()
                .map(SellerProfileDto::fromUser)
                .collect(Collectors.toList());
        return CompanyDashboardDto.fromCompany(company, sellerDtos, sellers.size());
    }

    @Override
    @Transactional
    public void createSeller(CreateSellerRequest request, UserDetails adminDetails) {
        // ... (existing createSeller logic)
        User adminUser = (User) adminDetails;
        Company company = companyRepo.findById(adminUser.getCompanyId())
                .orElseThrow(() -> new IllegalStateException("Admin is not associated with a valid company."));

        int currentSellerCount = userRepo.countSellersByCompanyId(company.getId());
        if (currentSellerCount >= company.getMaxSellers()) {
            throw new IllegalStateException("Seller limit reached for this company (" + company.getMaxSellers() + ").");
        }
        if (userRepo.findByEmail(request.getEmail()).isPresent()) {
            throw new IllegalStateException("Email already taken.");
        }

        User seller = new User();
        seller.setUserUuid(uuidService.generateUuid());
        seller.setFirstName(request.getFirstName());
        seller.setLastName(request.getLastName());
        seller.setUserName(request.getFirstName() + request.getLastName());
        seller.setEmail(request.getEmail());
        seller.setUserPassword(passwordEncoder.encode(request.getPassword()));
        seller.setRole("seller_company");
        seller.setCompanyId(adminUser.getCompanyId());
        seller.setStatus("active");

        userRepo.save(seller);
    }

    @Override
    @Transactional
    public void updateSeller(UUID sellerUuid, UpdateSellerRequest request, UserDetails adminDetails) {
        // ... (existing updateSeller logic)
        User adminUser = (User) adminDetails;
        User seller = userRepo.findByUuid(sellerUuid)
                .orElseThrow(() -> new IllegalStateException("Seller not found."));
        if (seller.getCompanyId() == null || !seller.getCompanyId().equals(adminUser.getCompanyId())) {
            throw new IllegalStateException("You do not have permission to modify this seller.");
        }
        seller.setFirstName(request.getFirstName());
        seller.setLastName(request.getLastName());
        seller.setStatus(request.getStatus());
        userRepo.updateSellerProfile(seller.getId(), seller.getFirstName(), seller.getLastName(), seller.getStatus());
    }

    @Override
    @Transactional
    public void deleteSeller(UUID sellerUuid, UserDetails adminDetails) {
        // ... (existing deleteSeller logic)
        User adminUser = (User) adminDetails;
        User seller = userRepo.findByUuid(sellerUuid)
                .orElseThrow(() -> new IllegalStateException("Seller not found."));
        if (seller.getCompanyId() == null || !seller.getCompanyId().equals(adminUser.getCompanyId())) {
            throw new IllegalStateException("You do not have permission to modify this seller.");
        }
        if (seller.getId().equals(adminUser.getId())) {
            throw new IllegalStateException("Admin cannot delete themselves.");
        }
        userRepo.deleteById(seller.getId());
    }

    // --- NEW METHOD IMPLEMENTATION ---
    @Override
    @Transactional
    public void updateCompanyProfile(UpdateCompanyRequest request, MultipartFile logoFile, UserDetails adminDetails) {
        User adminUser = (User) adminDetails;

        Company company = companyRepo.findById(adminUser.getCompanyId())
                .orElseThrow(() -> new IllegalStateException("Admin is not associated with a valid company."));

        // 1. Handle Logo File Upload (if provided)
        if (logoFile != null && !logoFile.isEmpty()) {
            String newLogoUrl = storageService.uploadFile(logoFile); // <-- Use Pinata service
            company.setLogoUrl(newLogoUrl);
        }

        // 2. Apply other partial updates
        if (request.getDescription() != null) {
            company.setDescription(request.getDescription());
        }
        if (request.getAddressLine1() != null) {
            company.setAddressLine1(request.getAddressLine1());
        }
        if (request.getCity() != null) {
            company.setCity(request.getCity());
        }
        if (request.getStateProvince() != null) {
            company.setStateProvince(request.getStateProvince());
        }
        if (request.getPostalCode() != null) {
            company.setPostalCode(request.getPostalCode());
        }
        if (request.getCountry() != null) {
            company.setCountry(request.getCountry());
        }

        companyRepo.updateCompanyProfile(company);
    }
}