package com.example.buyfast.modules.auth.dto;

import com.example.buyfast.modules.user.dto.UserResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthResponse {
    private String token;
    private Integer expiresIn;
    private UserResponse userResponse;
}