package com.example.buyfast.modules.auth.service.Impl;

// --- FIX: Add all these missing imports ---
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
// --- End of new imports ---

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
        user.setUserName(request.getFirstName()+request.getLastName());
        user.setEmail(request.getEmail());
        user.setUserPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole("buyer"); // Default role
        user.setStatus("pending"); // Await OTP verification

        userRepo.save(user);
        otpService.sendOtp(user.getEmail());
    }

    @Override
    @Transactional
    public AuthResponse login(LoginRequest request) {
        // This will throw an exception if auth fails (bad credentials or user disabled)
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        // If auth succeeds, fetch the user
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

        // Activate the user
        userRepo.updateUserStatus(request.getEmail(), "active");

        // Return a simple string message instead of a token
        return ResponseEntity.ok("Otp verified successfully");
    }

    @Override
    @Transactional
    public void requestPasswordReset(ForgotPasswordRequest request) {
        // Find the user
        Optional<User> userOpt = userRepo.findByEmail(request.getEmail());

        // Only send OTP if the user exists and is 'active'
        if (userOpt.isPresent() && "active".equals(userOpt.get().getStatus())) {
            otpService.sendOtp(request.getEmail());
        } else if (userOpt.isEmpty()) {
            // Fail silently or throw to avoid revealing if an email is registered
            throw new IllegalStateException("User not found.");
        } else {
            // User is 'pending' or 'banned'
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


        // 3. OTP is valid, find user (we know they exist)
        User user = userRepo.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalStateException("User not found.")); // Should not happen

        // 4. Encode and update the new password
        String encodedPassword = passwordEncoder.encode(request.getNewPassword());
        userRepo.updatePassword(user.getEmail(), encodedPassword);
    }
}