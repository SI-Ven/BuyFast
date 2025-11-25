package com.example.buyfast.modules.user.controller;

import com.example.buyfast.common.ApiResponse;
import com.example.buyfast.modules.user.dto.UpdateProfileRequest;
import com.example.buyfast.modules.user.model.User;
import com.example.buyfast.modules.user.model.UserProfile; // <-- NEW IMPORT
import com.example.buyfast.modules.user.service.UserService;
import com.example.buyfast.modules.verify.model.Verify;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
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

        // Use the service to get the User AND the Profile attached
        User fullUser = userService.getFullUserProfile(userDetails);

        return ResponseEntity.ok(ApiResponse.success("Profile retrieved successfully.", fullUser));
    }

    // --- MODIFIED ENDPOINT ---
    @PutMapping(value = "/profile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<UserProfile>> updateProfile(
            @RequestParam(value = "firstName", required = false) String firstName,
            @RequestParam(value = "lastName", required = false) String lastName,
            // userName and userProfile (string) removed from inputs
            @RequestParam(value = "dob", required = false) @DateTimeFormat(pattern = "MM-dd-yyyy") LocalDate dob,
            @RequestParam(value = "address", required = false) String address,
            @RequestParam(value = "phoneNumber", required = false) String phoneNumber,
            @RequestParam(value = "profilePictureFile", required = false) MultipartFile profilePictureFile,
            @AuthenticationPrincipal UserDetails userDetails) {

        // MODIFIED SERVICE CALL
        UserProfile updatedProfile = userService.updateUserProfile(
                firstName, lastName, dob, address, phoneNumber,
                userDetails, profilePictureFile);

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