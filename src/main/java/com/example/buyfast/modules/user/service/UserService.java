package com.example.buyfast.modules.user.service;

import com.example.buyfast.modules.user.dto.UpdateProfileRequest;
import com.example.buyfast.modules.user.model.User;
import com.example.buyfast.modules.user.model.UserProfile; // <-- NEW IMPORT
import com.example.buyfast.modules.verify.model.Verify;
import org.springframework.security.core.userdetails.UserDetails;

public interface UserService {

    // --- MODIFIED RETURN TYPE ---
    // Returns UserProfile as the user object itself doesn't change
    UserProfile updateUserProfile(UpdateProfileRequest request, UserDetails userDetails);

    Verify becomeSeller(UserDetails userDetails);

    void sendPhoneVerificationOtp(UserDetails userDetails);

    User verifyPhone(String otpCode, UserDetails userDetails);

    // --- NEW METHOD ---
    void deleteUser(UserDetails userDetails);
}