package com.example.buyfast.modules.user.controller;

import com.example.buyfast.modules.auth.dto.OtpRequest; // Re-using this DTO
import com.example.buyfast.modules.user.dto.UpdateProfileRequest;
import com.example.buyfast.modules.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PutMapping("/profile")
    public ResponseEntity<Map<String, String>> updateProfile(
            @Valid @RequestBody UpdateProfileRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        userService.updateUserProfile(request, userDetails);
        return ResponseEntity.ok(Map.of("message", "Profile updated successfully."));
    }

    @PostMapping("/become-seller")
    public ResponseEntity<Map<String, String>> becomeSeller(
            @AuthenticationPrincipal UserDetails userDetails) {

        userService.becomeSeller(userDetails);
        return ResponseEntity.ok(Map.of("message", "Congratulations, you are now a seller!"));
    }

    // --- NEW ENDPOINTS FOR PHONE VERIFICATION ---

    /**
     * Sends a verification OTP to the user's registered phone number.
     */
    @PostMapping("/phone/send-otp")
    public ResponseEntity<Map<String, String>> sendPhoneOtp(
            @AuthenticationPrincipal UserDetails userDetails) {

        userService.sendPhoneVerificationOtp(userDetails);
        return ResponseEntity.ok(Map.of("message", "OTP sent to your phone number."));
    }

    /**
     * Verifies the phone OTP.
     * We can re-use the OtpRequest DTO, but we only care about the 'otpCode' field.
     */
    @PostMapping("/phone/verify-otp")
    public ResponseEntity<Map<String, String>> verifyPhoneOtp(
            @Valid @RequestBody Map<String, String> payload,
            @AuthenticationPrincipal UserDetails userDetails) {

        String otpCode = payload.get("otpCode");
        if (otpCode == null || otpCode.isBlank()) {
            throw new IllegalStateException("otpCode is required.");
        }

        userService.verifyPhone(otpCode, userDetails);
        return ResponseEntity.ok(Map.of("message", "Phone number verified successfully."));
    }
}