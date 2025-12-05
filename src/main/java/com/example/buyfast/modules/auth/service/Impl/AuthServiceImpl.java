package com.example.buyfast.modules.auth.service.Impl;

import com.example.buyfast.config.JwtService;
import com.example.buyfast.modules.auth.dto.*;
import com.example.buyfast.modules.auth.model.Role;
import com.example.buyfast.modules.auth.repository.RoleRepo;
import com.example.buyfast.modules.auth.service.AuthService;
import com.example.buyfast.modules.company.model.Company;
import com.example.buyfast.modules.company.repository.CompanyRepo;
import com.example.buyfast.modules.otp.service.OtpService;
import com.example.buyfast.modules.storage.service.StorageService;
import com.example.buyfast.modules.user.model.User;
import com.example.buyfast.modules.user.model.UserProfile;
import com.example.buyfast.modules.user.repository.UserProfileRepo;
import com.example.buyfast.modules.user.repository.UserRepo;
import com.example.buyfast.modules.verify.model.Verify;
import com.example.buyfast.modules.verify.repository.VerifyRepo;
import com.example.buyfast.util.UuidService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepo userRepo;
    private final UserProfileRepo userProfileRepo;
    private final RoleRepo roleRepo;
    private final CompanyRepo companyRepo;
    private final VerifyRepo verifyRepo;

    private final UuidService uuidService;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final OtpService otpService;
    private final StorageService storageService;

    @Override
    @Transactional
    public User register(RegisterRequest request) {
        String email = request.getEmail().toLowerCase().trim();

        // 1. Check email
        if (userRepo.findByEmail(email).isPresent()) {
            throw new IllegalStateException("Email already taken");
        }

        // 2. Create User
        User user = new User();
        user.setUserUuid(uuidService.generateUuid());
        user.setEmail(email);
        user.setUserPassword(passwordEncoder.encode(request.getPassword()));
        user.setStatus("active");
        user.setCompanyId(null);
        // Removed setPhoneVerified(false)
        user.setVerified(false); // ID Card verification defaults to false

        // This save MUST generate the ID
        userRepo.save(user);

        // 3. Create User Profile immediately
        UserProfile profile = new UserProfile();
        profile.setUserId(user.getId());
        profile.setFirstName(request.getFirstName());
        profile.setLastName(request.getLastName());
        profile.setUserName(request.getFirstName() + request.getLastName());
        profile.setEmail(request.getEmail());
        userProfileRepo.create(profile);

        // 4. Assign Role
        Role buyerRole = roleRepo.findByRoleName("buyer")
                .orElseThrow(() -> new IllegalStateException("Default role 'buyer' not found. Please seed the DB."));
        roleRepo.insertUserRole(user.getId(), buyerRole.getId());

        // 5. Send OTP
        otpService.sendOtp(email);

        // 6. Return user
        user.setUserProfile(profile);
        return user;
    }

    @Override
    @Transactional
    public User registerCompany(RegisterCompanyRequest request, MultipartFile logoFile) {
        String email = request.getEmail().toLowerCase().trim();

        // 1. Check email
        if (userRepo.findByEmail(email).isPresent()) {
            throw new IllegalStateException("Email already taken");
        }

        // 2. Upload Logo
        String logoUrl = (logoFile != null && !logoFile.isEmpty()) ? storageService.uploadFile(logoFile) : null;

        // 3. Create User
        User user = new User();
        user.setUserUuid(uuidService.generateUuid());
        user.setEmail(email);
        user.setUserPassword(passwordEncoder.encode(request.getPassword()));
        user.setStatus("pending");
        user.setCompanyId(null);
        // Removed setPhoneVerified(false)
        user.setVerified(false);

        userRepo.save(user);

        // 4. Create User Profile immediately
        UserProfile profile = new UserProfile();
        profile.setUserId(user.getId());
        profile.setFirstName(request.getFirstName());
        profile.setLastName(request.getLastName());
        profile.setUserName(request.getFirstName() + request.getLastName());

        userProfileRepo.create(profile);

        // 5. Create Company
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

        // 6. Link User to Company
        user.setCompanyId(company.getId());
        userRepo.updateUserCompanyId(user.getId(), company.getId());

        // 7. Assign Admin Role
        Role adminRole = roleRepo.findByRoleName("admin_company")
                .orElseThrow(() -> new IllegalStateException("Role 'admin_company' not found."));
        roleRepo.insertUserRole(user.getId(), adminRole.getId());

        // 8. Create Verification Request for Company
        Verify verification = new Verify();
        verification.setVerifyUuid(uuidService.generateUuid());
        verification.setTargetType("company");
        verification.setTargetId(company.getCompanyUuid());
        verification.setSubmittedBy(user.getId());
        verification.setStatus("pending");

        verifyRepo.createVerification(verification);

        // 9. Send OTP
        otpService.sendOtp(email);

        user.setUserProfile(profile);
        return user;
    }

    @Override
    @Transactional
    public User verifyOtp(OtpRequest request) {
        String email = request.getEmail().toLowerCase().trim();
        boolean isValid = otpService.verifyOtp(email, request.getOtpCode());

        if (!isValid) {
            throw new IllegalStateException("Invalid or expired OTP");
        }

        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("User not found after OTP verification."));

        user.setStatus("active");
        userRepo.updateUserStatus(email, "active");

        return user;
    }

    @Override
    @Transactional
    public AuthResponse login(LoginRequest request) {
        String email = request.getEmail().toLowerCase().trim();

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, request.getPassword())
        );

        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("User not found after successful auth."));

        userRepo.updateLastLogin(user.getId());

        // Ensure your JwtService handles tokenVersion
        String jwtToken = jwtService.generateToken(user);

        return new AuthResponse(jwtToken);
    }

    @Override
    @Transactional
    public void requestPasswordReset(ForgotPasswordRequest request) {
        String email = request.getEmail().toLowerCase().trim();
        Optional<User> userOpt = userRepo.findByEmail(email);

        if (userOpt.isPresent() && "active".equals(userOpt.get().getStatus())) {
            otpService.sendOtp(email);
        } else if (userOpt.isEmpty()) {
            throw new IllegalStateException("User not found.");
        } else {
            throw new IllegalStateException("Account is not active.");
        }
    }

    @Override
    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        String email = request.getEmail().toLowerCase().trim();

        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new IllegalStateException("Passwords do not match.");
        }

        boolean isValid = otpService.verifyOtp(email, request.getOtpCode());
        if (!isValid) {
            throw new IllegalStateException("Invalid or expired OTP.");
        }

        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("User not found."));

        String encodedPassword = passwordEncoder.encode(request.getNewPassword());
        userRepo.updatePassword(user.getEmail(), encodedPassword);

        // Invalidate existing tokens
        logoutAll(user);
    }

    @Override
    @Transactional
    public void logoutAll(UserDetails userDetails) {
        User user = (User) userDetails;
        // Invalidates all old tokens by bumping the version
        userRepo.incrementTokenVersion(user.getId());
    }
}