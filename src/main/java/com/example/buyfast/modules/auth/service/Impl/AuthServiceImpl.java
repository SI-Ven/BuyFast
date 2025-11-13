package com.example.buyfast.modules.auth.service.Impl;

import com.example.buyfast.config.JwtService;
import com.example.buyfast.modules.auth.dto.*;
import com.example.buyfast.modules.auth.model.Role; // <-- NEW IMPORT
import com.example.buyfast.modules.auth.repository.RoleRepo; // <-- NEW IMPORT
import com.example.buyfast.modules.auth.service.AuthService;
import com.example.buyfast.modules.company.model.Company;
import com.example.buyfast.modules.company.repository.CompanyRepo;
import com.example.buyfast.modules.storage.service.StorageService;
import com.example.buyfast.modules.user.model.User;
import com.example.buyfast.modules.user.model.UserProfile;
import com.example.buyfast.modules.user.repository.UserRepo;
import com.example.buyfast.modules.user.repository.UserProfileRepo;
import com.example.buyfast.modules.otp.service.OtpService;
import com.example.buyfast.modules.verify.model.Verify;
import com.example.buyfast.modules.verify.repository.VerifyRepo;
import com.example.buyfast.util.UuidService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepo userRepo;
    private final UuidService uuidService;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final OtpService otpService;
    private final CompanyRepo companyRepo;
    private final StorageService storageService;
    private final VerifyRepo verifyRepo;
    private final UserProfileRepo userProfileRepo;
    private final RoleRepo roleRepo; // <-- NEW: Inject RoleRepo

    @Override
    @Transactional
    public User register(RegisterRequest request) {
        if (userRepo.findByEmail(request.getEmail()).isPresent()) {
            throw new IllegalStateException("Email already taken");
        }

        // 1. Create User object (Auth fields)
        User user = new User();
        user.setUserUuid(uuidService.generateUuid());
        user.setEmail(request.getEmail());
        user.setUserPassword(passwordEncoder.encode(request.getPassword()));
        user.setStatus("pending");
        user.setCompanyId(null);

        userRepo.save(user); // Save user to get the generated ID

        // 2. Create UserProfile object (Profile fields)
        UserProfile profile = new UserProfile();
        profile.setUserId(user.getId());
        profile.setFirstName(request.getFirstName());
        profile.setLastName(request.getLastName());
        profile.setUserName(request.getFirstName() + request.getLastName());
        userProfileRepo.create(profile);

        // 3. Assign Role (FIXED)
        Role buyerRole = roleRepo.findByRoleName("buyer")
                .orElseThrow(() -> new IllegalStateException("Default role 'buyer' not found. Please seed the DB."));
        roleRepo.insertUserRole(user.getId(), buyerRole.getId());

        // 4. Send OTP
        otpService.sendOtp(user.getEmail());

        // 5. Return user (with profile attached)
        user.setUserProfile(profile);
        return user;
    }

    @Override
    @Transactional
    public User registerCompany(RegisterCompanyRequest request, MultipartFile logoFile) {
        if (userRepo.findByEmail(request.getEmail()).isPresent()) {
            throw new IllegalStateException("Email already taken");
        }

        String logoUrl = null;
        if (logoFile != null && !logoFile.isEmpty()) {
            logoUrl = storageService.uploadFile(logoFile);
        }

        // 1. Create User (Auth fields)
        User user = new User();
        user.setUserUuid(uuidService.generateUuid());
        user.setEmail(request.getEmail());
        user.setUserPassword(passwordEncoder.encode(request.getPassword()));
        user.setStatus("pending");
        user.setCompanyId(null);
        userRepo.save(user);

        // 2. Create UserProfile (Profile fields)
        UserProfile profile = new UserProfile();
        profile.setUserId(user.getId());
        profile.setFirstName(request.getFirstName());
        profile.setLastName(request.getLastName());
        profile.setUserName(request.getFirstName() + request.getLastName());
        userProfileRepo.create(profile);

        // 3. Create Company
        Company company = new Company();
        company.setCompanyUuid(uuidService.generateUuid());
        company.setCompanyName(request.getCompanyName());
        company.setIndustryType(request.getIndustryType());
        company.setLogoUrl(logoUrl);
        company.setDescription(request.getDescription());
        company.setAddressLine1(request.getAddressLine1());
        company.setCity(request.getCity());
        company.setStateProvince(request.getStateProvince());
        company.setPostalCode(request.getPostalCode());
        company.setCountry(request.getCountry());
        company.setCreatedBy(user.getId());
        company.setMaxSellers(3);
        company.setStatus("pending");
        companyRepo.insert(company);

        // 4. Link user to company & Assign Role
        user.setCompanyId(company.getId());
        userRepo.updateUserCompanyId(user.getId(), company.getId());

        // --- FIXED: Assign role using RoleRepo ---
        Role adminRole = roleRepo.findByRoleName("admin_company")
                .orElseThrow(() -> new IllegalStateException("Role 'admin_company' not found."));
        roleRepo.insertUserRole(user.getId(), adminRole.getId());
        // -----------------------------------------

        // 5. Create Verification Request for the Company
        Verify verification = new Verify();
        verification.setVerifyUuid(uuidService.generateUuid());
        verification.setTargetType("company");
        verification.setTargetId(company.getCompanyUuid());
        verification.setSubmittedBy(user.getId());
        verification.setStatus("pending");
        verifyRepo.createVerification(verification);

        // 6. Send email OTP for user activation
        otpService.sendOtp(user.getEmail());

        // 7. Return user (with profile attached)
        user.setUserProfile(profile);
        return user;
    }

    @Override
    @Transactional
    public User verifyOtp(OtpRequest request) {
        boolean isValid = otpService.verifyOtp(request.getEmail(), request.getOtpCode());

        if (!isValid) {
            throw new IllegalStateException("Invalid or expired OTP");
        }

        User user = userRepo.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalStateException("User not found after OTP verification."));

        user.setStatus("active");
        userRepo.updateUserStatus(request.getEmail(), "active");

        return user;
    }

    @Override
    @Transactional
    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        User user = userRepo.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalStateException("User not found after successful auth."));

        userRepo.updateLastLogin(user.getId());
        String jwtToken = jwtService.generateToken(user);
        return new AuthResponse(jwtToken);
    }

    @Override
    @Transactional
    public void requestPasswordReset(ForgotPasswordRequest request) {
        Optional<User> userOpt = userRepo.findByEmail(request.getEmail());

        if (userOpt.isPresent() && "active".equals(userOpt.get().getStatus())) {
            otpService.sendOtp(request.getEmail());
        } else if (userOpt.isEmpty()) {
            throw new IllegalStateException("User not found.");
        } else {
            throw new IllegalStateException("Account is not active.");
        }
    }

    @Override
    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new IllegalStateException("Passwords do not match.");
        }

        boolean isValid = otpService.verifyOtp(request.getEmail(), request.getOtpCode());
        if (!isValid) {
            throw new IllegalStateException("Invalid or expired OTP.");
        }

        User user = userRepo.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalStateException("User not found."));

        String encodedPassword = passwordEncoder.encode(request.getNewPassword());
        userRepo.updatePassword(user.getEmail(), encodedPassword);
    }
}