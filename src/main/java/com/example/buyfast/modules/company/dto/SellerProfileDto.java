package com.example.buyfast.modules.company.dto;

import com.example.buyfast.modules.user.model.User;
import com.example.buyfast.modules.user.model.UserProfile; // <-- Make sure this is imported
import lombok.Data;
import java.util.UUID;

@Data
public class SellerProfileDto {
    private UUID userUuid;
    private String firstName;
    private String lastName;
    private String userName;
    private String email;
    private String status;
    private String userProfile; // Avatar URL

    /**
     * Factory method to create DTO from the User entity.
     * This is the method that needed to be fixed.
     */
    public static SellerProfileDto fromUser(User user) {
        SellerProfileDto dto = new SellerProfileDto();

        // --- Fields from User object ---
        dto.setUserUuid(user.getUserUuid());
        dto.setEmail(user.getEmail());
        dto.setStatus(user.getStatus());

        // --- FIX: Fields from nested UserProfile object ---
        // Add a null check in case the profile doesn't exist yet
        if (user.getUserProfile() != null) {
            UserProfile profile = user.getUserProfile();
            dto.setFirstName(profile.getFirstName());
            dto.setLastName(profile.getLastName());
            dto.setUserName(profile.getUserName());
            dto.setUserProfile(profile.getUserProfile()); // This is the avatar URL
        }

        return dto;
    }
}