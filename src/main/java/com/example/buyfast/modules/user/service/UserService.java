package com.example.buyfast.modules.user.service;

import com.example.buyfast.modules.user.dto.UpdateProfileRequest;
import com.example.buyfast.modules.user.model.User;
import org.springframework.security.core.userdetails.UserDetails;

public interface UserService {

    User updateUserProfile(UpdateProfileRequest request, UserDetails userDetails);

    // --- MODIFIED ---
    User becomeSeller(UserDetails userDetails);

    void sendPhoneVerificationOtp(UserDetails userDetails);

    User verifyPhone(String otpCode, UserDetails userDetails); // <-- MODIFIED
}