package com.example.buyfast.modules.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.time.LocalDate;

@Data
public class RegisterRequest {
    @NotBlank
    private String firstName;
    @NotBlank
    private String lastName;

    private LocalDate dob;
    private String address;

    @NotBlank
    @Email
    private String email;

    @NotBlank
    @Size(min = 8)
    private String password;
}