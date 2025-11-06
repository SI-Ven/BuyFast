package com.example.buyfast.modules.user.service;

import com.example.buyfast.modules.user.dto.UpdateProfileRequest;
import org.springframework.security.core.userdetails.UserDetails;

public interface UserService {

    void updateUserProfile(UpdateProfileRequest request, UserDetails userDetails);

    void becomeSeller(UserDetails userDetails);

    // --- NEW METHODS ---
    void sendPhoneVerificationOtp(UserDetails userDetails);

    void verifyPhone(String otpCode, UserDetails userDetails);
}