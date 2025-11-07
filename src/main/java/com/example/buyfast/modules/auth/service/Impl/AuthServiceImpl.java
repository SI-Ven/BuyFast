package com.example.buyfast.modules.auth.service.Impl;

import com.example.buyfast.config.JwtService;
import com.example.buyfast.modules.auth.dto.*;
import com.example.buyfast.modules.auth.service.AuthService;
import com.example.buyfast.modules.company.model.Company;
import com.example.buyfast.modules.company.repository.CompanyRepo;
import com.example.buyfast.modules.storage.service.StorageService;
import com.example.buyfast.modules.user.model.User;
import com.example.buyfast.modules.user.repository.UserRepo;
import com.example.buyfast.modules.otp.service.OtpService;
import com.example.buyfast.util.UuidService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
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
    private final StorageService storageService; // <-- Injected

    @Override
    @Transactional
    public void register(RegisterRequest request) {
        // ... (existing register logic)
        if (userRepo.findByEmail(request.getEmail()).isPresent()) {
            throw new IllegalStateException("Email already taken");
        }

        User user = new User();
        user.setUserUuid(uuidService.generateUuid());
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setUserName(request.getFirstName() + request.getLastName());
        user.setEmail(request.getEmail());
        user.setUserPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole("buyer"); // Default role
        user.setStatus("pending"); // Await OTP verification
        user.setCompanyId(null);

        userRepo.save(user);
        otpService.sendOtp(user.getEmail());
    }

    @Override
    @Transactional
    public void registerCompany(RegisterCompanyRequest request, MultipartFile logoFile) { // <-- MODIFIED
        // 1. Check if email is already taken
        if (userRepo.findByEmail(request.getEmail()).isPresent()) {
            throw new IllegalStateException("Email already taken");
        }

        // 2. Upload Logo (if provided)
        String logoUrl = null;
        if (logoFile != null && !logoFile.isEmpty()) {
            logoUrl = storageService.uploadFile(logoFile); // <-- Use Pinata service
        }

        // 3. Create the User first
        User user = new User();
        user.setUserUuid(uuidService.generateUuid());
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setUserName(request.getFirstName() + request.getLastName());
        user.setEmail(request.getEmail());
        user.setUserPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole("admin_company"); // <-- Correct role
        user.setStatus("pending");
        user.setCompanyId(null);
        userRepo.save(user);

        // 4. Create the Company
        Company company = new Company();
        company.setCompanyUuid(uuidService.generateUuid());
        company.setCompanyName(request.getCompanyName());
        company.setIndustryType(request.getIndustryType());
        company.setLogoUrl(logoUrl); // <-- SET THE UPLOADED URL
        company.setDescription(request.getDescription());
        company.setAddressLine1(request.getAddressLine1());
        company.setCity(request.getCity());
        company.setStateProvince(request.getStateProvince());
        company.setPostalCode(request.getPostalCode());
        company.setCountry(request.getCountry());
        company.setCreatedBy(user.getId());
        company.setMaxSellers(3);
        company.setStatus("active");
        companyRepo.insert(company);

        // 5. Link the User to their new Company
        userRepo.updateUserCompanyId(user.getId(), company.getId());

        // 6. Send OTP
        otpService.sendOtp(user.getEmail());
    }

    @Override
    @Transactional
    public AuthResponse login(LoginRequest request) {
        // ... (existing login logic)
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
    public ResponseEntity<String> verifyOtp(OtpRequest request) {
        // ... (existing verifyOtp logic)
        boolean isValid = otpService.verifyOtp(request.getEmail(), request.getOtpCode());

        if (!isValid) {
            throw new IllegalStateException("Invalid or expired OTP");
        }
        userRepo.updateUserStatus(request.getEmail(), "active");
        return ResponseEntity.ok("Otp verified successfully");
    }

    @Override
    @Transactional
    public void requestPasswordReset(ForgotPasswordRequest request) {
        // ... (existing requestPasswordReset logic)
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
        // ... (existing resetPassword logic)
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