package com.example.buyfast.modules.user.service.Impl;

import com.example.buyfast.modules.otp.service.SmsOtpService;
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
    private final SmsOtpService smsOtpService;

    // --- UNCHANGED (Already returns User) ---
    @Override
    @Transactional
    public User updateUserProfile(UpdateProfileRequest request, UserDetails userDetails) {
        User currentUser = (User) userDetails;

        if (request.getFirstName() != null) {
            currentUser.setFirstName(request.getFirstName());
        }
        if (request.getLastName() != null) {
            currentUser.setLastName(request.getLastName());
        }
        if (request.getUserName() != null) {
            currentUser.setUserName(request.getUserName());
        }
        if (request.getUserProfile() != null) {
            currentUser.setUserProfile(request.getUserProfile());
        }
        if (request.getDob() != null) {
            currentUser.setDob(request.getDob());
        }
        if (request.getAddress() != null) {
            currentUser.setAddress(request.getAddress());
        }

        if (request.getPhoneNumber() != null && !request.getPhoneNumber().equals(currentUser.getPhoneNumber())) {
            currentUser.setPhoneVerified(false);
            currentUser.setPhoneNumber(request.getPhoneNumber());
        }

        userRepo.updateProfile(currentUser);

        return currentUser;
    }

    // --- MODIFIED ---
    @Override
    @Transactional
    public User becomeSeller(UserDetails userDetails) {
        User currentUser = (User) userDetails;

        if (!"buyer".equals(currentUser.getRole())) {
            throw new IllegalStateException("Only buyers can become sellers.");
        }

        userRepo.updateUserRole(currentUser.getId(), "seller");
        currentUser.setRole("seller"); // <-- Update object in memory
        return currentUser; // <-- RETURN USER
    }

    // --- UNCHANGED ---
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

    // --- MODIFIED ---
    @Override
    @Transactional
    public User verifyPhone(String otpCode, UserDetails userDetails) {
        User currentUser = (User) userDetails;
        if (currentUser.getPhoneNumber() == null) {
            throw new IllegalStateException("No phone number found to verify.");
        }

        boolean isValid = smsOtpService.verifyOtp(currentUser.getPhoneNumber(), otpCode);

        if (!isValid) {
            throw new IllegalStateException("Invalid or expired OTP.");
        }

        userRepo.setPhoneVerified(currentUser.getId());
        currentUser.setPhoneVerified(true); // <-- Update object in memory
        return currentUser; // <-- RETURN USER
    }
}