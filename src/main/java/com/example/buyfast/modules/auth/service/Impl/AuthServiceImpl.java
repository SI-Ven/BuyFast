package com.example.buyfast.modules.auth.service.Impl;

import com.example.buyfast.config.JwtService;
import com.example.buyfast.modules.auth.dto.*;
import com.example.buyfast.modules.auth.service.AuthService;
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

    // ... [register, login, verifyOtp, requestPasswordReset methods are correct] ...

    @Override
    @Transactional
    public void register(RegisterRequest request) {
        if (userRepo.findByEmail(request.getEmail()).isPresent()) {
            throw new IllegalStateException("Email already taken");
        }

        User user = new User();
        user.setUserUuid(uuidService.generateUuid());
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setUserName(request.getFirstName() + request.getLastName()); // From your file
        user.setEmail(request.getEmail());
        user.setUserPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole("buyer"); // Default role
        user.setStatus("pending"); // Await OTP verification

        userRepo.save(user); // This transaction will now commit
        otpService.sendOtp(user.getEmail()); // This will save the OTP
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
    public ResponseEntity<String> verifyOtp(OtpRequest request) {
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
        // 1. Check if passwords match
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new IllegalStateException("Passwords do not match.");
        }

        // --- THIS STEP WAS MISSING ---
        // 2. Verify the OTP
        boolean isValid = otpService.verifyOtp(request.getEmail(), request.getOtpCode());
        if (!isValid) {
            throw new IllegalStateException("Invalid or expired OTP.");
        }
        // --- END OF FIX ---

        // 3. OTP is valid, find user (we know they exist)
        User user = userRepo.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalStateException("User not found."));

        // 4. Encode and update the new password
        String encodedPassword = passwordEncoder.encode(request.getNewPassword());
        userRepo.updatePassword(user.getEmail(), encodedPassword);
    }
}