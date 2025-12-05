package com.example.buyfast.modules.user.service;

import com.example.buyfast.modules.user.dto.UpdateProfileRequest;
import com.example.buyfast.modules.user.model.User;
import com.example.buyfast.modules.user.model.UserProfile; // <-- NEW IMPORT
import com.example.buyfast.modules.verify.model.Verify;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;

public interface UserService {

    // --- MODIFIED RETURN TYPE ---
    // Returns UserProfile as the user object itself doesn't change
    UserProfile updateUserProfile(
            String firstName,
            String lastName,
            String email,       // <-- Added
            String address,
            String city,        // <-- Added
            String country,     // <-- Added
            String postalCode,  // <-- Added
            String phoneNumber,
            UserDetails userDetails,
            MultipartFile profilePictureFile);
    Verify becomeSeller(UserDetails userDetails);

    void sendPhoneVerificationOtp(UserDetails userDetails);

    User verifyPhone(String otpCode, UserDetails userDetails);

    // --- NEW METHOD ---
    void deleteUser(UserDetails userDetails);

    User getFullUserProfile(UserDetails userDetails);
}