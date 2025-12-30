package com.example.buyfast.modules.user.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

@Data
public class UpdateProfileRequest {

    private String firstName;
    private String lastName;
    private String userName;
    private String email;
    private String phoneNumber;
    private String userProfile; // Avatar URL
    private String address;
    private String city;
    private String country;
    private String postalCode;
}