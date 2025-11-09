package com.example.buyfast.modules.company.service.Impl;

import com.example.buyfast.modules.company.dto.*;
import com.example.buyfast.modules.company.model.Company;
import com.example.buyfast.modules.company.repository.CompanyRepo;
import com.example.buyfast.modules.company.service.CompanyService;
import com.example.buyfast.modules.storage.service.StorageService;
import com.example.buyfast.modules.user.model.User;
import com.example.buyfast.modules.user.repository.UserRepo;
import com.example.buyfast.modules.verify.model.Verify; // <-- NEW IMPORT
import com.example.buyfast.modules.verify.repository.VerifyRepo; // <-- NEW IMPORT
import com.example.buyfast.util.UuidService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

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
    private final StorageService storageService;
    private final VerifyRepo verifyRepo; // <-- NEW: Inject VerifyRepo

    /**
     * Helper method to get the admin's company AND verify it is active.
     */
    private Company getActiveCompanyForAdmin(UserDetails adminDetails) {
        User adminUser = (User) adminDetails;
        Company company = companyRepo.findById(adminUser.getCompanyId())
                .orElseThrow(() -> new IllegalStateException("Admin is not associated with a valid company."));

        if (!"active".equals(company.getStatus())) {
            throw new IllegalStateException("Your company registration is not yet approved. Status: " + company.getStatus());
        }
        return company;
    }

    // --- MODIFIED ---
    @Override
    @Transactional
    public Company createCompany(CreateCompanyRequest request, UserDetails adminDetails) {
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
        company.setLogoUrl(request.getLogoUrl());
        company.setDescription(request.getDescription());
        company.setCreatedBy(adminUser.getId());
        company.setMaxSellers(3);
        company.setStatus("pending"); // <-- FIX: Set status to pending

        companyRepo.insert(company);

        // Link user to company
        userRepo.updateUserRoleAndCompany(adminUser.getId(), "admin_company", company.getId());

        // --- NEW: Create Verification Request ---
        Verify verification = new Verify();
        verification.setVerifyUuid(uuidService.generateUuid());
        verification.setTargetType("company");
        verification.setTargetId(company.getCompanyUuid());
        verification.setSubmittedBy(adminUser.getId());
        verification.setStatus("pending");
        verifyRepo.createVerification(verification);
        // --- END NEW ---

        return company;
    }

    // --- MODIFIED ---
    @Override
    public CompanyDashboardDto getCompanyDashboard(UserDetails adminDetails) {
        // --- SECURITY CHECK ---
        Company company = getActiveCompanyForAdmin(adminDetails);

        List<User> sellers = userRepo.findSellersByCompanyId(company.getId());
        List<SellerProfileDto> sellerDtos = sellers.stream()
                .map(SellerProfileDto::fromUser)
                .collect(Collectors.toList());
        return CompanyDashboardDto.fromCompany(company, sellerDtos, sellers.size());
    }

    // --- MODIFIED ---
    @Override
    @Transactional
    public User createSeller(CreateSellerRequest request, UserDetails adminDetails) {
        // --- SECURITY CHECK ---
        Company company = getActiveCompanyForAdmin(adminDetails);
        User adminUser = (User) adminDetails; // We still need the user object

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
        return seller;
    }

    // --- MODIFIED ---
    @Override
    @Transactional
    public User updateSeller(UUID sellerUuid, UpdateSellerRequest request, UserDetails adminDetails) {
        // --- SECURITY CHECK ---
        Company adminCompany = getActiveCompanyForAdmin(adminDetails);
        User adminUser = (User) adminDetails; // We still need the user object

        User seller = userRepo.findByUuid(sellerUuid)
                .orElseThrow(() -> new IllegalStateException("Seller not found."));

        // Check if seller belongs to the admin's company
        if (seller.getCompanyId() == null || !seller.getCompanyId().equals(adminCompany.getId())) {
            throw new IllegalStateException("You do not have permission to modify this seller.");
        }

        seller.setFirstName(request.getFirstName());
        seller.setLastName(request.getLastName());
        seller.setStatus(request.getStatus());
        userRepo.updateSellerProfile(seller.getId(), seller.getFirstName(), seller.getLastName(), seller.getStatus());
        return seller;
    }

    // --- MODIFIED ---
    @Override
    @Transactional
    public User deleteSeller(UUID sellerUuid, UserDetails adminDetails) {
        // --- SECURITY CHECK ---
        Company adminCompany = getActiveCompanyForAdmin(adminDetails);
        User adminUser = (User) adminDetails; // We still need the user object

        User seller = userRepo.findByUuid(sellerUuid)
                .orElseThrow(() -> new IllegalStateException("Seller not found."));

        // Check if seller belongs to the admin's company
        if (seller.getCompanyId() == null || !seller.getCompanyId().equals(adminCompany.getId())) {
            throw new IllegalStateException("You do not have permission to modify this seller.");
        }
        if (seller.getId().equals(adminUser.getId())) {
            throw new IllegalStateException("Admin cannot delete themselves.");
        }

        userRepo.deleteById(seller.getId());
        return seller;
    }


    // --- MODIFIED ---
    @Override
    @Transactional
    public Company updateCompanyProfile(UpdateCompanyRequest request, MultipartFile logoFile, UserDetails adminDetails) {
        // --- SECURITY CHECK ---
        Company company = getActiveCompanyForAdmin(adminDetails);
        // User adminUser = (User) adminDetails; // Not needed here

        // This block correctly handles a null or empty file
        if (logoFile != null && !logoFile.isEmpty()) {
            String newLogoUrl = storageService.uploadFile(logoFile);
            company.setLogoUrl(newLogoUrl);
        }

        // These blocks correctly handle partial JSON updates
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

        return company;
    }
}