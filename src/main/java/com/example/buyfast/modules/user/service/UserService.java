package com.example.buyfast.modules.user.service;

import com.example.buyfast.modules.user.dto.UpdateProfileRequest;
import com.example.buyfast.modules.user.model.User;
import com.example.buyfast.modules.verify.model.Verify; // <-- NEW IMPORT
import org.springframework.security.core.userdetails.UserDetails;

public interface UserService {

    User updateUserProfile(UpdateProfileRequest request, UserDetails userDetails);

    // --- MODIFIED ---
    Verify becomeSeller(UserDetails userDetails); // <-- Returns Verify object

    void sendPhoneVerificationOtp(UserDetails userDetails);

    User verifyPhone(String otpCode, UserDetails userDetails);
}