package com.example.buyfast.modules.user.service;

import com.example.buyfast.modules.user.dto.UpdateProfileRequest;
import com.example.buyfast.modules.user.model.User;
import com.example.buyfast.modules.user.model.UserProfile;
import com.example.buyfast.modules.verify.model.Verify;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;

public interface UserService {

    UserProfile updateUserProfile(
            String firstName,
            String lastName,
            String email,
            String address,
            String city,
            String country,
            String postalCode,
            String phoneNumber,
            UserDetails userDetails,
            MultipartFile profilePictureFile,
            MultipartFile coverProfileFile); // Added Argument

    Verify becomeSeller(UserDetails userDetails);

    void deleteUser(UserDetails userDetails);

    User getFullUserProfile(UserDetails userDetails);
}