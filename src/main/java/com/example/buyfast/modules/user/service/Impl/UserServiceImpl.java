package com.example.buyfast.modules.user.service.Impl;

import com.example.buyfast.modules.otp.service.SmsOtpService; // <-- NEW IMPORT
import com.example.buyfast.modules.user.dto.UpdateProfileRequest;
import com.example.buyfast.modules.user.model.User;
import com.example.buyfast.modules.user.repository.UserRepo;
import com.example.buyfast.modules.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepo userRepo;
    private final SmsOtpService smsOtpService; // <-- NEW INJECTION

    @Override
    @Transactional
    public void updateUserProfile(UpdateProfileRequest request, UserDetails userDetails) {
        User currentUser = (User) userDetails;

        // Check if phone number is being changed
        if (request.getPhoneNumber() != null && !request.getPhoneNumber().equals(currentUser.getPhoneNumber())) {
            // If phone changed, mark it as unverified
            currentUser.setPhoneVerified(false);
            currentUser.setPhoneNumber(request.getPhoneNumber());
        }

        currentUser.setFirstName(request.getFirstName());
        currentUser.setLastName(request.getLastName());
        currentUser.setUserName(request.getUserName());
        currentUser.setDob(request.getDob());
        currentUser.setAddress(request.getAddress());

        userRepo.updateProfile(currentUser);
    }

    // ... [becomeSeller method remains the same] ...
    @Override
    @Transactional
    public void becomeSeller(UserDetails userDetails) {
        User currentUser = (User) userDetails;

        if (!"buyer".equals(currentUser.getRole())) {
            throw new IllegalStateException("Only buyers can become sellers.");
        }

        userRepo.updateUserRole(currentUser.getId(), "seller");
    }

    // --- NEW METHODS FOR PHONE VERIFICATION ---

    /**
     * Triggers sending an OTP to the user's saved phone number.
     */
    @Override
    public void sendPhoneVerificationOtp(UserDetails userDetails) {
        User currentUser = (User) userDetails;
        if (currentUser.getPhoneNumber() == null || currentUser.getPhoneNumber().isEmpty()) {
            throw new IllegalStateException("Please add a phone number to your profile first.");
        }
        if (currentUser.isPhoneVerified()) {
            throw new IllegalStateException("Phone number is already verified.");
        }
        smsOtpService.sendOtp(currentUser.getPhoneNumber());
    }

    /**
     * Verifies the OTP and marks the user's phone as verified.
     */
    @Override
    @Transactional
    public void verifyPhone(String otpCode, UserDetails userDetails) {
        User currentUser = (User) userDetails;
        if (currentUser.getPhoneNumber() == null) {
            throw new IllegalStateException("No phone number found to verify.");
        }

        boolean isValid = smsOtpService.verifyOtp(currentUser.getPhoneNumber(), otpCode);

        if (!isValid) {
            throw new IllegalStateException("Invalid or expired OTP.");
        }

        // Update the user's verification status in the users table
        userRepo.setPhoneVerified(currentUser.getId());
    }
}