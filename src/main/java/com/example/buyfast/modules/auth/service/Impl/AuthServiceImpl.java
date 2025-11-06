package com.example.buyfast.modules.auth.service.Impl;

import com.example.buyfast.config.JwtService;
import com.example.buyfast.modules.auth.dto.AuthResponse;
import com.example.buyfast.modules.auth.dto.LoginRequest;
import com.example.buyfast.modules.auth.dto.OtpRequest;
import com.example.buyfast.modules.auth.dto.RegisterRequest;
import com.example.buyfast.modules.auth.service.AuthService; // <-- Import interface
import com.example.buyfast.modules.user.model.User;
import com.example.buyfast.modules.user.repository.UserRepo;
import com.example.buyfast.modules.otp.service.OtpService;
import com.example.buyfast.util.UuidService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService { // <-- Implements interface

    private final UserRepo userRepo;
    private final UuidService uuidService;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final OtpService otpService;

    @Override // <-- Add annotation
    @Transactional
    public void register(RegisterRequest request) {
        if (userRepo.findByEmail(request.getEmail()).isPresent()) {
            throw new IllegalStateException("Email already taken");
        }

        User user = new User();
        user.setUserUuid(uuidService.generateUuid());
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setDob(request.getDob());
        user.setAddress(request.getAddress());
        user.setEmail(request.getEmail());
        user.setUserPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole("buyer");
        user.setStatus("pending");

        userRepo.save(user);
        otpService.sendOtp(user.getEmail());
    }

    @Override // <-- Add annotation
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

    @Override // <-- Add annotation
    @Transactional
    public AuthResponse verifyOtp(OtpRequest request) {
        boolean isValid = otpService.verifyOtp(request.getEmail(), request.getOtpCode());

        if (!isValid) {
            throw new IllegalStateException("Invalid or expired OTP");
        }

        userRepo.updateUserStatus(request.getEmail(), "active");

        User user = userRepo.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalStateException("User not found after verification."));

        String jwtToken = jwtService.generateToken(user);
        return new AuthResponse(jwtToken);
    }
}