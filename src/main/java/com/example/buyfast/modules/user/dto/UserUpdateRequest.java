package com.example.buyfast.modules.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserUpdateRequest {
    private String username;
    private String email;
    private String password;
    private Boolean isActive;
    private String roleName;
}
