package com.example.buyfast.modules.company.service.Impl;

import com.example.buyfast.modules.auth.model.Role;
import com.example.buyfast.modules.auth.repository.RoleRepo;
import com.example.buyfast.modules.company.dto.*;
import com.example.buyfast.modules.company.model.Company;
import com.example.buyfast.modules.company.repository.CompanyRepo;
import com.example.buyfast.modules.company.service.CompanyService;
import com.example.buyfast.modules.storage.service.StorageService;
import com.example.buyfast.modules.user.model.User;
import com.example.buyfast.modules.user.model.UserProfile;
import com.example.buyfast.modules.user.repository.UserProfileRepo;
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
    private final UserProfileRepo userProfileRepo;
    private final RoleRepo roleRepo;
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

    @Override
    public CompanyDashboardDto getCompanyDashboard(UserDetails adminDetails) {
        Company company = getActiveCompanyForAdmin(adminDetails);

        List<User> sellers = userRepo.findSellersByCompanyId(company.getId());

        List<SellerProfileDto> sellerDtos = sellers.stream()
                .map(SellerProfileDto::fromUser)
                .collect(Collectors.toList());

        int sellerCount = userRepo.countSellersByCompanyId(company.getId());

        return CompanyDashboardDto.fromCompany(company, sellerDtos, sellerCount);
    }

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

        // 1. Create User (Auth fields)
        User newSeller = new User();
        newSeller.setUserUuid(uuidService.generateUuid());
        newSeller.setEmail(request.getEmail());
        newSeller.setUserPassword(passwordEncoder.encode(request.getPassword()));
        newSeller.setCompanyId(adminCompany.getId());
        newSeller.setStatus("active");

        userRepo.save(newSeller); // Save to get the generated ID

        // 2. Create UserProfile (Profile fields)
        UserProfile profile = new UserProfile();
        profile.setUserId(newSeller.getId());
        profile.setFirstName(request.getFirstName());
        profile.setLastName(request.getLastName());
        profile.setUserName(request.getFirstName() + request.getLastName());
        userProfileRepo.create(profile);

        // 3. Assign Role (Fixed: using RoleRepo)
        Role sellerRole = roleRepo.findByRoleName("seller_company")
                .orElseThrow(() -> new IllegalStateException("Role 'seller_company' not found."));
        roleRepo.insertUserRole(newSeller.getId(), sellerRole.getId());

        newSeller.setUserProfile(profile);
        return newSeller;
    }

    @Override
    @Transactional
    public User updateSeller(UUID sellerUuid, UpdateSellerRequest request, UserDetails adminDetails) {
        Company adminCompany = getActiveCompanyForAdmin(adminDetails);

        User seller = userRepo.findByUuid(sellerUuid)
                .orElseThrow(() -> new IllegalStateException("Seller not found."));

        // Security Check: ensure seller belongs to the admin's company
        if (!adminCompany.getId().equals(seller.getCompanyId())) {
            throw new IllegalStateException("You do not have permission to modify this seller.");
        }

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

        // 3. Persist User changes
        if (userUpdated) {
            userRepo.updateUserStatus(seller.getEmail(), seller.getStatus());
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

        // Security Check: ensure seller belongs to the admin's company
        if (!adminCompany.getId().equals(seller.getCompanyId())) {
            throw new IllegalStateException("You do not have permission to delete this seller.");
        }

        // Prevent admin from deleting themselves (just in case)
        if (adminUser.getId().equals(seller.getId())) {
            throw new IllegalStateException("Admin cannot delete themselves.");
        }

        // Delete profile and then user
        userProfileRepo.deleteByUserId(seller.getId());
        userRepo.deleteById(seller.getId());

        // Note: If you have a join table for roles (user_roles) without cascade delete,
        // you might need to call roleRepo.deleteUserRoles(seller.getId()) here too.

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

    @Override
    @Transactional
    public Company createCompany(CreateCompanyRequest request, MultipartFile logoFile, UserDetails adminDetails) {
        User adminUser = (User) adminDetails;

        // 1. Validation
        if (adminUser.getCompanyId() != null) {
            throw new IllegalStateException("User is already part of a company.");
        }
        if (companyRepo.findByAdminId(adminUser.getId()).isPresent()) {
            throw new IllegalStateException("User already owns a company.");
        }

        // 2. Upload Logo
        String logoUrl = null;
        if (logoFile != null && !logoFile.isEmpty()) {
            logoUrl = storageService.uploadFile(logoFile);
        }

        // 3. Create Company Entity
        Company company = new Company();
        company.setCompanyUuid(uuidService.generateUuid());
        company.setCompanyName(request.getCompanyName());
        company.setIndustryType(request.getIndustryType());
        company.setTaxId(request.getTaxId());
        company.setPhoneNumber(request.getPhoneNumber());
        company.setDescription(request.getDescription());
        company.setLogoUrl(logoUrl);

        // Address
        company.setAddressLine1(request.getAddressLine1());
        company.setCity(request.getCity());
        company.setStateProvince(request.getStateProvince());
        company.setPostalCode(request.getPostalCode());
        company.setCountry(request.getCountry());

        company.setCreatedBy(adminUser.getId());
        company.setMaxSellers(3); // Default limit
        company.setStatus("pending");

        companyRepo.insert(company);

        // 4. Update User (Link to company)
        userRepo.updateUserCompanyId(adminUser.getId(), company.getId());

        // 5. Assign 'admin_company' role
        Role adminRole = roleRepo.findByRoleName("admin_company")
                .orElseThrow(() -> new IllegalStateException("Role 'admin_company' not found."));
        roleRepo.insertUserRole(adminUser.getId(), adminRole.getId());

        // 6. Create Verification Request
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
    public List<User> getCompanySellers(UserDetails adminDetails) {
        // 1. Validate the admin and get their company
        Company company = getActiveCompanyForAdmin(adminDetails);

        // 2. Fetch users with 'seller_company' role for this company
        // This relies on the method already existing in your UserRepo
        return userRepo.findSellersByCompanyId(company.getId());
    }
}