package com.example.buyfast.modules.user.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
// import java.sql.Date; // <-- REMOVE THIS
import java.time.LocalDate; // <-- ADD THIS
import java.sql.Timestamp;

@Data
public class UserProfile {
    @JsonIgnore
    private Long id;
    @JsonIgnore
    private Long userId; // Foreign key to users.id
    private String firstName;
    private String lastName;
    private String userName;
    private String email;
    private String phoneNumber;
    private String userProfile; // Avatar URL
    private String coverProfile;
    private String address;
    private String city;
    private String country;
    private String postalCode;
    private Timestamp createdAt;
    private Timestamp updatedAt;
}

