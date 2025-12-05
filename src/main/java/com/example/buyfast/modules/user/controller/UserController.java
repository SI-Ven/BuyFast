package com.example.buyfast.modules.user.controller;

import com.example.buyfast.common.ApiResponse;
import com.example.buyfast.modules.user.model.User;
import com.example.buyfast.modules.user.model.UserProfile;
import com.example.buyfast.modules.user.service.UserService;
import com.example.buyfast.modules.verify.model.Verify;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<User>> getProfile(
            @AuthenticationPrincipal UserDetails userDetails) {
        User fullUser = userService.getFullUserProfile(userDetails);
        return ResponseEntity.ok(ApiResponse.success("Profile retrieved successfully.", fullUser));
    }

    @PutMapping(value = "/profile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<UserProfile>> updateProfile(
            @RequestParam(value = "firstName", required = false) String firstName,
            @RequestParam(value = "lastName", required = false) String lastName,
            @RequestParam(value = "email", required = false) String email,
            @RequestParam(value = "address", required = false) String address,
            @RequestParam(value = "city", required = false) String city,
            @RequestParam(value = "country", required = false) String country,
            @RequestParam(value = "postalCode", required = false) String postalCode,
            @RequestParam(value = "phoneNumber", required = false) String phoneNumber,
            @RequestParam(value = "profilePictureFile", required = false) MultipartFile profilePictureFile,
            @AuthenticationPrincipal UserDetails userDetails) {

        UserProfile updatedProfile = userService.updateUserProfile(
                firstName, lastName, email, address, city, country, postalCode,
                phoneNumber, userDetails, profilePictureFile);

        return ResponseEntity.ok(ApiResponse.success("Profile updated successfully.", updatedProfile));
    }

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




}