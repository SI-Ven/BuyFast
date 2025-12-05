package com.example.buyfast.modules.user.service.Impl;

import com.example.buyfast.modules.storage.service.StorageService;
import com.example.buyfast.modules.user.model.User;
import com.example.buyfast.modules.user.model.UserProfile;
import com.example.buyfast.modules.user.repository.UserProfileRepo;
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

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepo userRepo;
    private final UserProfileRepo userProfileRepo;
    private final VerifyRepo verifyRepo;
    private final UuidService uuidService;
    private final StorageService storageService;

    @Override
    @Transactional
    public UserProfile updateUserProfile(
            String firstName,
            String lastName,
            String email,
            String address,
            String city,
            String country,
            String postalCode,
            String phoneNumber,
            UserDetails userDetails,
            MultipartFile profilePictureFile) {

        User currentUser = (User) userDetails;

        // 1. Get or Create UserProfile
        UserProfile profile = userProfileRepo.findByUserId(currentUser.getId())
                .orElseGet(() -> {
                    UserProfile newProfile = new UserProfile();
                    newProfile.setUserId(currentUser.getId());
                    // Set defaults from User entity if available
                    newProfile.setEmail(currentUser.getEmail());
                    // Removed: newProfile.setPhoneNumber(...) as User no longer has phoneNumber
                    userProfileRepo.create(newProfile);
                    return newProfile;
                });

        boolean profileUpdated = false;

        // 2. Update Fields on UserProfile
        if (firstName != null) { profile.setFirstName(firstName); profileUpdated = true; }
        if (lastName != null) { profile.setLastName(lastName); profileUpdated = true; }
        if (email != null) { profile.setEmail(email); profileUpdated = true; }
        if (address != null) { profile.setAddress(address); profileUpdated = true; }
        if (city != null) { profile.setCity(city); profileUpdated = true; }
        if (country != null) { profile.setCountry(country); profileUpdated = true; }
        if (postalCode != null) { profile.setPostalCode(postalCode); profileUpdated = true; }
        if (phoneNumber != null) { profile.setPhoneNumber(phoneNumber); profileUpdated = true; }

        // --- Automatically set username (First + Last) ---
        String currentFirstName = profile.getFirstName() != null ? profile.getFirstName() : "";
        String currentLastName = profile.getLastName() != null ? profile.getLastName() : "";
        String newUserName = (currentFirstName + " " + currentLastName).trim();

        if (!newUserName.equals(profile.getUserName())) {
            profile.setUserName(newUserName);
            profileUpdated = true;
        }

        // Removed: Section 3 that updated User table phone number (columns removed from DB)

        // 3. Handle File Upload
        if (profilePictureFile != null && !profilePictureFile.isEmpty()) {
            String newProfilePicUrl = storageService.uploadFile(profilePictureFile);
            profile.setUserProfile(newProfilePicUrl);
            profileUpdated = true;
        }

        // 4. Persist changes
        if (profileUpdated) {
            userProfileRepo.update(profile);
        }

        currentUser.setUserProfile(profile);
        return profile;
    }

    @Override
    @Transactional
    public Verify becomeSeller(UserDetails userDetails) {
        User currentUser = (User) userDetails;

        boolean isBuyer = currentUser.getRoles().stream()
                .anyMatch(role -> "buyer".equals(role.getRoleName()));

        if (!isBuyer) {
            throw new IllegalStateException("Only buyers can become sellers.");
        }

        verifyRepo.findPendingRequest(currentUser.getUserUuid(), "seller").ifPresent(v -> {
            throw new IllegalStateException("You already have a pending seller request.");
        });

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
    @Transactional
    public void deleteUser(UserDetails userDetails) {
        User currentUser = (User) userDetails;
        userRepo.deleteById(currentUser.getId());
    }

    @Override
    public User getFullUserProfile(UserDetails userDetails) {
        User currentUser = (User) userDetails;
        userProfileRepo.findByUserId(currentUser.getId())
                .ifPresent(profile -> currentUser.setUserProfile(profile));
        return currentUser;
    }
}