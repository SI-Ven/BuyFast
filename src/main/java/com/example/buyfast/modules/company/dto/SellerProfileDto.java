package com.example.buyfast.modules.company.dto;

import com.example.buyfast.modules.user.model.User;
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
    private String userProfile;

    public static SellerProfileDto fromUser(User user) {
        SellerProfileDto dto = new SellerProfileDto();
        dto.setUserUuid(user.getUserUuid());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setUserName(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setStatus(user.getStatus());
        dto.setUserProfile(user.getUserProfile());
        return dto;
    }
}