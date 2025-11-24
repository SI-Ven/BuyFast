package com.example.buyfast.modules.auth.service;

import com.example.buyfast.common.service.RedisService;
import com.example.buyfast.modules.auth.dto.*;
import com.example.buyfast.modules.auth.model.Otp;
import com.example.buyfast.modules.auth.repository.OtpRepository;
import com.example.buyfast.modules.notification.EmailService;
import com.example.buyfast.modules.user.dto.RegisterRequest;
import com.example.buyfast.modules.user.dto.UserResponse;
import com.example.buyfast.modules.user.model.Permission;
import com.example.buyfast.modules.user.model.User;
import com.example.buyfast.modules.user.repository.UserRepository;
import com.example.buyfast.modules.user.service.UserService;
import com.example.buyfast.security.jwt.JwtService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserService userService;
    private final UserRepository userRepository; // Direct access needed for full entity load
    private final EmailService emailService;
    private final OtpRepository otpRepository;
    private final RedisService redisService;

    // --- 1. REGISTER: Just send OTP, DO NOT return token yet ---
    public void registerBuyer(RegisterRequest request) {
        registerUserWithOtp(request, "USER");
    }

    public void registerSupplier(RegisterRequest request) {
        registerUserWithOtp(request, "COMPANY_OWNER");
    }

    private void registerUserWithOtp(RegisterRequest request, String roleName) {
        // 1. Save User (Enabled = False)
        userService.registerUser(request, roleName);
        // 2. Send OTP
        generateAndSendOtp(request.getEmail(), "Verification Otp");
    }

    // --- 2. VERIFY OTP: This is where they get the FIRST token ---
    public AuthResponse verifyOtp(VerifyOtpRequest request) {
        // A. Check OTP
        Otp otp = otpRepository.findByEmailAndOtpCode(request.getEmail(), request.getOtp())
                .orElseThrow(() -> new IllegalArgumentException("Invalid or expired OTP"));

        if (otp.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("OTP has expired");
        }

        if (Boolean.TRUE.equals(otp.getIsUsed())) {
            throw new IllegalArgumentException("OTP already used");
        }

        // B. Mark OTP used
        otpRepository.markAsUsed(otp.getId());

        // C. Enable the User (Activate Account)
        userService.enableUser(request.getEmail());

        // D. Auto-Login the user immediately
        // We fetch the full entity to get permissions for Redis
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));
        return processLogin(user);
    }

    // --- 3. LOGIN: Standard password login ---
    public AuthResponse login(LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );
        } catch (AuthenticationException e) {
            log.warn("Login failed for email [{}]: {}", request.getEmail(), e.getMessage());
            throw e;
        }

        // Fetch FULL User entity (User -> Roles -> Permissions)
        // We cannot use UserService.getUserByEmail because that returns a DTO (UserResponse)
        // We need the deep permission structure here.
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));
        return processLogin(user);
    }
    public void resendOtp(ResendOtpRequest request) {
        if (!userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("User not found");
        }
        generateAndSendOtp(request.getEmail(), "Resend OTP");
    }

    public void forgotPassword(ForgotPasswordRequest request) {
        if (!userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("User not found");
        }
        generateAndSendOtp(request.getEmail(), "Password Reset");
    }

    public void resetPassword(ResetPasswordRequest request) {
        verifyOtpLogic(request.getEmail(), request.getOtp());

        // 2. Get User ID (Keep this read logic here or move to verifyOtpLogic return)
        User user = userRepository.findByUsername(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 3. Delegate the update to UserService
        // Pass the RAW password. UserService handles the encoding.
        userService.updatePassword(user.getId(), request.getNewPassword());

        log.info("Password reset successfully for user: {}", request.getEmail());
    }

    private void verifyOtpLogic(String email, String otpCode) {
        Otp otp = otpRepository.findByEmailAndOtpCode(email, otpCode)
                .orElseThrow(() -> new IllegalArgumentException("Invalid or expired OTP"));

        if (otp.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("OTP has expired");
        }
        if (Boolean.TRUE.equals(otp.getIsUsed())) {
            throw new IllegalArgumentException("OTP already used");
        }

        otpRepository.markAsUsed(otp.getId());
    }

    // --- HELPER: Common logic for Verify OTP and Normal Login ---
    private AuthResponse processLogin(User user) {
        // 1. Extract Permissions from the Entity
        List<String> permissionKeys = user.getRoles().stream()
                .filter(role -> role != null && Boolean.TRUE.equals(role.getIsActive()))
                .flatMap(role -> {
                    // FIX: Change Set<Permission> to List<Permission>
                    List<Permission> perms = role.getPermissions();

                    // Flatten the list of permissions
                    return perms == null ? java.util.stream.Stream.empty() : perms.stream();
                })
                .map(Permission::getPermissionKey) // Extract "user:create" string
                .distinct()
                .collect(Collectors.toList());

        // 2. Save to Redis (Key: "user_perms:UUID")
        String redisKey = "user_perms:" + user.getPublicId().toString();
        redisService.setValue(redisKey, permissionKeys, 24, TimeUnit.HOURS);

        // 3. Generate Token (Using UserResponse DTO structure if expected, or construct manual)
        // We need to map User entity to UserResponse for the JwtService (which expects UserResponse)
        // Or simply create a minimal object if JwtService only needs PublicID.

        // Assuming your JwtService.generateToken takes UserResponse:
        // We can use a quick mapper here or fetch the DTO.
        // Since we have the entity, let's just create a UserResponse wrapper for the token generator.
        UserResponse userDto = UserResponse.builder()
                .publicId(user.getPublicId().toString())
                .email(user.getEmail())
                .username(user.getUsername())
                .build();

        String jwtToken = jwtService.generateToken(userDto);

        // 4. Return Response
        // (We might want to fetch the full DTO properly mapped for the response payload)
        UserResponse fullResponse = userService.getUserByEmail(user.getEmail());

        return AuthResponse.builder()
                .token(jwtToken)
                .expiresIn(86400000) // 24 hours in ms
                .userResponse(fullResponse)
                .build();
    }

    public void generateAndSendOtp(String email, String subjectType) {
        String otpCode = String.format("%06d", new Random().nextInt(999999));
        otpRepository.deleteByEmail(email); // Clear old OTPs

        Otp otp = Otp.builder()
                .email(email)
                .otpCode(otpCode)
                .isUsed(false)
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusMinutes(5)) // Give them 5 minutes
                .build();
        otpRepository.insert(otp);

        String emailSubject = "BuyFast-" + subjectType;
        String emailBody = buildOtpEmailBody(otpCode);

        emailService.sendEmail(email, emailSubject, emailBody);
        log.info("OTP sent to: {}", email);
    }

    private String buildOtpEmailBody(String otp) {
        return """
            Hello,
            Here is your verification code for BuyFast:
            %s
            This code will expire in 5 minutes.
            If you did not request this code, please ignore this email.
            """.formatted(otp);
    }
}