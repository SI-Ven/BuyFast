package com.example.buyfast.modules.user.controller;

import com.example.buyfast.common.ApiResponse;
import com.example.buyfast.modules.user.dto.UpdateProfileRequest;
import com.example.buyfast.modules.user.model.User;
import com.example.buyfast.modules.user.model.UserProfile; // <-- NEW IMPORT
import com.example.buyfast.modules.user.service.UserService;
import com.example.buyfast.modules.verify.model.Verify;
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

    // --- NEW ENDPOINT ---
    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<User>> getProfile(
            @AuthenticationPrincipal UserDetails userDetails) {
        // The User object from @AuthenticationPrincipal is fully populated
        // by the UserDetailsService, which should include the UserProfile.
        User currentUser = (User) userDetails;
        return ResponseEntity.ok(ApiResponse.success("Profile retrieved successfully.", currentUser));
    }

    // --- MODIFIED ENDPOINT ---
    @PutMapping("/profile")
    public ResponseEntity<ApiResponse<UserProfile>> updateProfile( // <-- Return UserProfile
                                                                   @Valid @RequestBody UpdateProfileRequest request,
                                                                   @AuthenticationPrincipal UserDetails userDetails) {

        UserProfile updatedProfile = userService.updateUserProfile(request, userDetails);
        return ResponseEntity.ok(ApiResponse.success("Profile updated successfully.", updatedProfile));
    }

    // --- NEW ENDPOINT ---
    @DeleteMapping("/profile")
    public ResponseEntity<ApiResponse<Object>> deleteProfile(
            @AuthenticationPrincipal UserDetails userDetails) {
        userService.deleteUser(userDetails);
        return ResponseEntity.ok(ApiResponse.ok("User account deleted successfully."));
    }

    @PostMapping("/become-seller")
    public ResponseEntity<ApiResponse<Verify>> becomeSeller(
            @AuthenticationPrincipal UserDetails userDetails) {

        Verify verificationRequest = userService.becomeSeller(userDetails);
        return ResponseEntity.ok(ApiResponse.success("Your request to become a seller has been submitted for approval.", verificationRequest));
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