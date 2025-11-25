package com.example.buyfast.modules.user.service.Impl;

import com.example.buyfast.modules.otp.service.SmsOtpService;
import com.example.buyfast.modules.storage.service.StorageService;
import com.example.buyfast.modules.user.model.User;
import com.example.buyfast.modules.user.model.UserProfile; // <-- NEW IMPORT
import com.example.buyfast.modules.user.repository.UserProfileRepo; // <-- NEW IMPORT
import com.example.buyfast.modules.user.repository.UserRepo;
import com.example.buyfast.modules.user.service.UserService;
import com.example.buyfast.modules.verify.model.Verify;
import com.example.buyfast.modules.verify.repository.VerifyRepo;
import com.example.buyfast.util.UuidService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepo userRepo;
    private final UserProfileRepo userProfileRepo; // <-- NEW: Inject UserProfileRepo
    private final SmsOtpService smsOtpService;
    private final VerifyRepo verifyRepo;
    private final UuidService uuidService;
    private final StorageService storageService;

    // --- HEAVILY MODIFIED ---
    @Override
    @Transactional
    public UserProfile updateUserProfile( // MODIFIED SIGNATURE
                                          String firstName,
                                          String lastName,
                                          LocalDate dob,
                                          String address,
                                          String phoneNumber,
                                          UserDetails userDetails,
                                          MultipartFile profilePictureFile) {

        User currentUser = (User) userDetails;

        // 1. Get or Create UserProfile
        UserProfile profile = userProfileRepo.findByUserId(currentUser.getId())
                .orElseGet(() -> {
                    UserProfile newProfile = new UserProfile();
                    newProfile.setUserId(currentUser.getId());
                    userProfileRepo.create(newProfile);
                    return newProfile;
                });

        // 2. Update Profile Fields
        boolean profileUpdated = false;

        // --- Update individual fields ---
        if (firstName != null) {
            profile.setFirstName(firstName);
            profileUpdated = true;
        }
        if (lastName != null) {
            profile.setLastName(lastName);
            profileUpdated = true;
        }

        // --- NEW LOGIC: Automatically set username ---
        String currentFirstName = profile.getFirstName() != null ? profile.getFirstName() : "";
        String currentLastName = profile.getLastName() != null ? profile.getLastName() : "";
        String newUserName = currentFirstName + currentLastName;

        if (!newUserName.equals(profile.getUserName())) {
            profile.setUserName(newUserName);
            profileUpdated = true;
        }
        // ---------------------------------------------

        if (dob != null) {
            profile.setDob(dob);
            profileUpdated = true;
        }
        if (address != null) {
            profile.setAddress(address);
            profileUpdated = true;
        }

        // Handle phone number update and verification status
        if (phoneNumber != null && !phoneNumber.equals(currentUser.getPhoneNumber())) {
            currentUser.setPhoneNumber(phoneNumber);
            currentUser.setPhoneVerified(false);
            userRepo.updateUserPhoneNumber(currentUser.getId(), phoneNumber);
        }

        // 3. Handle File Upload (using 'profilePictureFile' for the actual upload)
        if (profilePictureFile != null && !profilePictureFile.isEmpty()) {
            String newProfilePicUrl = storageService.uploadFile(profilePictureFile);
            profile.setUserProfile(newProfilePicUrl); // userProfile field holds the avatar URL
            profileUpdated = true;
        }
        // NOTE: The previous string 'userProfile' input is no longer supported as an input parameter.

        // 4. Persist the profile changes
        if (profileUpdated) {
            userProfileRepo.update(profile);
        }

        // 5. Return the updated profile
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

    // --- NEW METHOD ---
    @Override
    @Transactional
    public void deleteUser(UserDetails userDetails) {
        User currentUser = (User) userDetails;
        userRepo.deleteById(currentUser.getId());
    }

    @Override
    public User getFullUserProfile(UserDetails userDetails) {
        User currentUser = (User) userDetails;

        // Fetch the profile manually using the Repo
        userProfileRepo.findByUserId(currentUser.getId())
                .ifPresent(profile -> currentUser.setUserProfile(profile));

        return currentUser;
    }

    @Override
    public void sendPhoneVerificationOtp(UserDetails userDetails) {
        // ... (unchanged)
        User currentUser = (User) userDetails;
        if (currentUser.getPhoneNumber() == null || currentUser.getPhoneNumber().isEmpty()) {
            throw new IllegalStateException("Please add a phone number to your profile first.");
        }
        if (currentUser.getPhoneVerified()) {
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
}