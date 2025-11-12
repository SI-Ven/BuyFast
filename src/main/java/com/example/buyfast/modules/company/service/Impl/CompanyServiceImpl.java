package com.example.buyfast.modules.company.service.Impl;

// --- Import all the new models and repos ---
import com.example.buyfast.modules.auth.model.Role;
import com.example.buyfast.modules.company.dto.*;
import com.example.buyfast.modules.company.model.Company;
import com.example.buyfast.modules.company.repository.CompanyRepo;
import com.example.buyfast.modules.company.service.CompanyService;
import com.example.buyfast.modules.storage.service.StorageService;
import com.example.buyfast.modules.user.model.User;
import com.example.buyfast.modules.user.model.UserProfile; // <-- NEW IMPORT
import com.example.buyfast.modules.user.repository.UserProfileRepo; // <-- NEW IMPORT
import com.example.buyfast.modules.user.repository.UserRepo;
import com.example.buyfast.modules.verify.model.Verify;
import com.example.buyfast.modules.verify.repository.VerifyRepo;
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
    private final UserProfileRepo userProfileRepo; // <-- NEW: Inject UserProfileRepo
    private final UuidService uuidService;
    private final PasswordEncoder passwordEncoder;
    private final StorageService storageService;
    private final VerifyRepo verifyRepo;

    /**
     * Helper method to get the admin's company and ensure it's active.
     */
    private Company getActiveCompanyForAdmin(UserDetails adminDetails) {
        User adminUser = (User) adminDetails;
        if (adminUser.getCompanyId() == null) {
            throw new IllegalStateException("Admin is not associated with a valid company.");
        }

        Company company = companyRepo.findById(adminUser.getCompanyId())
                .orElseThrow(() -> new IllegalStateException("Admin's company not found."));

        if (!"active".equals(company.getStatus())) {
            throw new IllegalStateException("Your company registration is not yet approved. Status: " + company.getStatus());
        }
        return company;
    }

    // --- HEAVILY MODIFIED ---
    @Override
    @Transactional
    public Company createCompany(CreateCompanyRequest request, UserDetails adminDetails) {
        User adminUser = (User) adminDetails;

        // Check if user is already in a company
        if (adminUser.getCompanyId() != null) {
            throw new IllegalStateException("User is already part of a company.");
        }

        // --- FIX: This is the error at line 58 ---
        // We now check the Set<Role> instead of a single .getRole()
        boolean isEligible = adminUser.getRoles().stream()
                .anyMatch(role -> "buyer".equals(role.getRoleName()) ||
                        "seller".equals(role.getRoleName())); // Assuming 'seller' is the individual seller role

        if (!isEligible) {
            throw new IllegalStateException("Only buyers or individual sellers can create a new company.");
        }
        // --- END FIX ---

        // Check if user already owns a company
        if (companyRepo.findByAdminId(adminUser.getId()).isPresent()) {
            throw new IllegalStateException("User already owns a company.");
        }

        // 2. Create Company
        Company company = new Company();
        company.setCompanyUuid(uuidService.generateUuid());
        company.setCompanyName(request.getCompanyName());
        company.setIndustryType(request.getIndustryType());
        // logoUrl is handled by updateCompanyProfile
        company.setDescription(request.getDescription());
        company.setCreatedBy(adminUser.getId());
        company.setMaxSellers(3); // Default value
        company.setStatus("pending");
        companyRepo.insert(company);

        // 3. Link user to company & assign role
        // This line is based on your old UserRepo.java
        // You will need to refactor UserRepo to handle role changes in a new table
        userRepo.updateUserRoleAndCompany(adminUser.getId(), "admin_company", company.getId());


        // 4. Create Verification Request
        Verify verification = new Verify();
        verification.setVerifyUuid(uuidService.generateUuid());
        verification.setTargetType("company");
        verification.setTargetId(company.getCompanyUuid());
        verification.setSubmittedBy(adminUser.getId());
        verification.setStatus("pending");
        verifyRepo.createVerification(verification);

        return company;
    }

    @Override
    public CompanyDashboardDto getCompanyDashboard(UserDetails adminDetails) {
        Company company = getActiveCompanyForAdmin(adminDetails);
        List<User> sellers = userRepo.findSellersByCompanyId(company.getId());

        List<SellerProfileDto> sellerDtos = sellers.stream()
                .map(SellerProfileDto::fromUser) // Assumes SellerProfileDto is fixed
                .collect(Collectors.toList());

        int sellerCount = userRepo.countSellersByCompanyId(company.getId());

        return CompanyDashboardDto.fromCompany(company, sellerDtos, sellerCount);
    }

    // --- HEAVILY MODIFIED ---
    @Override
    @Transactional
    public User createSeller(CreateSellerRequest request, UserDetails adminDetails) {
        Company adminCompany = getActiveCompanyForAdmin(adminDetails);

        // Check seller limit
        int currentSellerCount = userRepo.countSellersByCompanyId(adminCompany.getId());
        if (currentSellerCount >= adminCompany.getMaxSellers()) {
            throw new IllegalStateException("Seller limit reached for this company (" + adminCompany.getMaxSellers() + ").");
        }

        if (userRepo.findByEmail(request.getEmail()).isPresent()) {
            throw new IllegalStateException("Email already taken.");
        }

        // --- FIX: Create User and UserProfile separately ---

        // 1. Create User (Auth fields)
        User newSeller = new User();
        newSeller.setUserUuid(uuidService.generateUuid());
        newSeller.setEmail(request.getEmail());
        newSeller.setUserPassword(passwordEncoder.encode(request.getPassword()));
        newSeller.setCompanyId(adminCompany.getId());
        newSeller.setStatus("active"); // Company sellers are active by default

        // This call will fail once you remove 'role' from the 'save' method in UserRepo
        // You must refactor UserRepo.save to remove the 'role' column
        userRepo.save(newSeller); // Save to get the generated ID

        // 2. Create UserProfile (Profile fields)
        UserProfile profile = new UserProfile();
        profile.setUserId(newSeller.getId());
        profile.setFirstName(request.getFirstName());
        profile.setLastName(request.getLastName());
        profile.setUserName(request.getFirstName() + request.getLastName());
        userProfileRepo.create(profile);

        // 3. Assign Role (This is the new way)
        // You need to implement RoleRepo.assignRoleToUser
        // roleRepo.assignRoleToUser(newSeller.getId(), "seller_company");

        // This is the old way, which must be removed when UserRepo is fixed
        userRepo.updateUserRole(newSeller.getId(), "seller_company");


        newSeller.setUserProfile(profile);
        return newSeller;
    }

    // --- HEAVILY MODIFIED ---
    @Override
    @Transactional
    public User updateSeller(UUID sellerUuid, UpdateSellerRequest request, UserDetails adminDetails) {
        Company adminCompany = getActiveCompanyForAdmin(adminDetails);

        User seller = userRepo.findByUuid(sellerUuid)
                .orElseThrow(() -> new IllegalStateException("Seller not found."));

        // Check if seller belongs to the admin's company
        if (!adminCompany.getId().equals(seller.getCompanyId())) {
            throw new IllegalStateException("You do not have permission to modify this seller.");
        }

        // --- FIX: Update User and UserProfile separately ---

        // 1. Update User object (Status)
        boolean userUpdated = false;
        if (request.getStatus() != null && !request.getStatus().equals(seller.getStatus())) {
            seller.setStatus(request.getStatus());
            userUpdated = true;
        }

        // 2. Update UserProfile object
        UserProfile profile = userProfileRepo.findByUserId(seller.getId())
                .orElseThrow(() -> new IllegalStateException("Seller profile not found."));

        boolean profileUpdated = false;
        if (request.getFirstName() != null) {
            profile.setFirstName(request.getFirstName());
            profileUpdated = true;
        }
        if (request.getLastName() != null) {
            profile.setLastName(request.getLastName());
            profileUpdated = true;
        }

        if (profileUpdated) {
            userProfileRepo.update(profile);
        }

        // 3. Persist User changes (if any)
        // We use the old repo method for now, which incorrectly bundles status and profile
        // This should be changed to userRepo.update(seller) once UserRepo is refactored
        if (userUpdated || profileUpdated) {
            userRepo.updateSellerProfile(
                    seller.getId(),
                    profile.getFirstName(),
                    profile.getLastName(),
                    seller.getStatus()
            );
        }

        seller.setUserProfile(profile);
        return seller;
    }

    @Override
    @Transactional
    public User deleteSeller(UUID sellerUuid, UserDetails adminDetails) {
        User adminUser = (User) adminDetails;
        Company adminCompany = getActiveCompanyForAdmin(adminDetails);

        User seller = userRepo.findByUuid(sellerUuid)
                .orElseThrow(() -> new IllegalStateException("Seller not found."));

        // Check if seller belongs to the admin's company
        if (!adminCompany.getId().equals(seller.getCompanyId())) {
            throw new IllegalStateException("You do not have permission to modify this seller.");
        }

        // Prevent admin from deleting themselves
        if (adminUser.getId().equals(seller.getId())) {
            throw new IllegalStateException("Admin cannot delete themselves.");
        }

        // --- FIX: Delete profile and then user ---
        userProfileRepo.deleteByUserId(seller.getId());
        userRepo.deleteById(seller.getId());

        return seller;
    }

    @Override
    @Transactional
    public Company updateCompanyProfile(UpdateCompanyRequest request, MultipartFile logoFile, UserDetails adminDetails) {
        Company company = getActiveCompanyForAdmin(adminDetails);

        // Upload new logo if provided
        if (logoFile != null && !logoFile.isEmpty()) {
            String newLogoUrl = storageService.uploadFile(logoFile);
            company.setLogoUrl(newLogoUrl);
        }

        // Update other company fields
        if (request.getCompanyName() != null) {
            company.setCompanyName(request.getCompanyName());
        }
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