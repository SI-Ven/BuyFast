package com.example.buyfast.modules.user.controller;

import com.example.buyfast.common.ApiResponse;
import com.example.buyfast.modules.user.dto.UpdateProfileRequest;
import com.example.buyfast.modules.user.model.User;
import com.example.buyfast.modules.user.service.UserService;
import com.example.buyfast.modules.verify.model.Verify; // <-- NEW IMPORT
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
    public ResponseEntity<ApiResponse<User>> updateProfile(
            @Valid @RequestBody UpdateProfileRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        User updatedUser = userService.updateUserProfile(request, userDetails);
        return ResponseEntity.ok(ApiResponse.success("Profile updated successfully.", updatedUser));
    }

    // --- MODIFIED ---
    @PostMapping("/become-seller")
    public ResponseEntity<ApiResponse<Verify>> becomeSeller( // <-- Changed to ApiResponse<Verify>
                                                             @AuthenticationPrincipal UserDetails userDetails) {

        Verify verificationRequest = userService.becomeSeller(userDetails);
        return ResponseEntity.ok(ApiResponse.success("Your request to become a seller has been submitted for approval.", verificationRequest)); // <-- Add payload
    }

    @PostMapping("/phone/send-otp")
    public ResponseEntity<ApiResponse<Object>> sendPhoneOtp(
            @AuthenticationPrincipal UserDetails userDetails) {

        userService.sendPhoneVerificationOtp(userDetails);
        return ResponseEntity.ok(ApiResponse.ok("OTP sent to your phone number."));
    }

    @PostMapping("/phone/verify-otp")
    public ResponseEntity<ApiResponse<User>> verifyPhoneOtp(
            @Valid @RequestBody Map<String, String> payload,
            @AuthenticationPrincipal UserDetails userDetails) {

        String otpCode = payload.get("otpCode");
        if (otpCode == null || otpCode.isBlank()) {
            throw new IllegalStateException("otpCode is required.");
        }

        User user = userService.verifyPhone(otpCode, userDetails);
        return ResponseEntity.ok(ApiResponse.success("Phone number verified successfully.", user));
    }
}