package com.example.buyfast.modules.user.service.Impl;

import com.example.buyfast.modules.otp.service.SmsOtpService;
import com.example.buyfast.modules.user.dto.UpdateProfileRequest;
import com.example.buyfast.modules.user.model.User;
import com.example.buyfast.modules.user.model.UserProfile;
import com.example.buyfast.modules.user.repository.UserProfileRepo;
import com.example.buyfast.modules.user.repository.UserRepo;
import com.example.buyfast.modules.user.service.UserService;
import com.example.buyfast.modules.verify.model.Verify; // <-- NEW IMPORT
import com.example.buyfast.modules.verify.repository.VerifyRepo; // <-- NEW IMPORT
import com.example.buyfast.util.UuidService; // <-- NEW IMPORT
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepo userRepo;
    private final SmsOtpService smsOtpService;
    private final UserProfileRepo userProfileRepo;
    private final VerifyRepo verifyRepo; // <-- NEW: Inject VerifyRepo
    private final UuidService uuidService;// <-- NEW: Inject UuidService

    @Override
    @Transactional
    public UserProfile updateUserProfile(UpdateProfileRequest request, UserDetails userDetails) {
        User currentUser = (User) userDetails;

        // 1. Get or Create UserProfile
        // This handles users who were created without a profile entry
        UserProfile profile = userProfileRepo.findByUserId(currentUser.getId())
                .orElseGet(() -> {
                    UserProfile newProfile = new UserProfile();
                    newProfile.setUserId(currentUser.getId());
                    userProfileRepo.create(newProfile); // Assumes 'create' sets the ID
                    return newProfile;
                });

        // 2. Update Profile Fields (on the UserProfile object)
        boolean profileUpdated = false;
        if (request.getFirstName() != null) {
            profile.setFirstName(request.getFirstName());
            profileUpdated = true;
        }
        if (request.getLastName() != null) {
            profile.setLastName(request.getLastName());
            profileUpdated = true;
        }
        if (request.getUserName() != null) {
            profile.setUserName(request.getUserName());
            profileUpdated = true;
        }
        if (request.getUserProfile() != null) {
            profile.setUserProfile(request.getUserProfile());
            profileUpdated = true;
        }
        if (request.getDob() != null) {
            profile.setDob(request.getDob());
            profileUpdated = true;
        }
        if (request.getAddress() != null) {
            profile.setAddress(request.getAddress());
            profileUpdated = true;
        }

        if (profileUpdated) {
            userProfileRepo.update(profile);
        }

        // 3. Update User Fields (on the User object)
        boolean userUpdated = false;
        if (request.getPhoneNumber() != null && !request.getPhoneNumber().equals(currentUser.getPhoneNumber())) {
            currentUser.setPhoneVerified(false);
            currentUser.setPhoneNumber(request.getPhoneNumber());
            userUpdated = true;
        }

        if (userUpdated) {
            userRepo.update(currentUser);
        }

        // 4. Return the updated profile
        // We also set it on the currentUser object for this request context
        currentUser.setUserProfile(profile);
        return profile;
    }

    // --- MODIFIED ---
    @Override
    @Transactional
    public Verify becomeSeller(UserDetails userDetails) {
        User currentUser = (User) userDetails;

        // --- BUG FIX ---
        // Changed from .getRole() to checking the Set of roles
        boolean isBuyer = currentUser.getRoles().stream()
                .anyMatch(role -> "buyer".equals(role.getRoleName()));

        if (!isBuyer) {
            throw new IllegalStateException("Only buyers can become sellers.");
        }

        // Check for existing pending request (Logic is unchanged)
        verifyRepo.findPendingRequest(currentUser.getUserUuid(), "seller").ifPresent(v -> {
            throw new IllegalStateException("You already have a pending seller request.");
        });

        // Create a verification request (Logic is unchanged)
        Verify verification = new Verify();
        verification.setVerifyUuid(uuidService.generateUuid());
        verification.setTargetType("seller");
        verification.setTargetId(currentUser.getUserUuid());
        verification.setSubmittedBy(currentUser.getId());
        verification.setStatus("pending");

        verifyRepo.createVerification(verification);
        return verification;
    }
    @Override
    public void sendPhoneVerificationOtp(UserDetails userDetails) {
        // ... (unchanged)
        User currentUser = (User) userDetails;
        if (currentUser.getPhoneNumber() == null || currentUser.getPhoneNumber().isEmpty()) {
            throw new IllegalStateException("Please add a phone number to your profile first.");
        }
        if (currentUser.isPhoneVerified()) {
            throw new IllegalStateException("Phone number is already verified.");
        }
        smsOtpService.sendOtp(currentUser.getPhoneNumber());
    }

    @Override
    @Transactional
    public User verifyPhone(String otpCode, UserDetails userDetails) {
        // ... (unchanged)
        User currentUser = (User) userDetails;
        if (currentUser.getPhoneNumber() == null) {
            throw new IllegalStateException("No phone number found to verify.");
        }

        boolean isValid = smsOtpService.verifyOtp(currentUser.getPhoneNumber(), otpCode);

        if (!isValid) {
            throw new IllegalStateException("Invalid or expired OTP.");
        }

        userRepo.setPhoneVerified(currentUser.getId());
        currentUser.setPhoneVerified(true);
        return currentUser;
    }

    @Override
    @Transactional
    public void deleteUser(UserDetails userDetails) {
        User currentUser = (User) userDetails;

        // 1. Delete associated profile
        userProfileRepo.deleteByUserId(currentUser.getId());

        // 2. Delete role associations (MyBatis/DB cascade should handle this)
        // If not, you need a UserRoleRepo.deleteByUserId(currentUser.getId())

        // 3. Delete the user
        userRepo.deleteById(currentUser.getId());
    }