package com.example.buyfast.modules.user.model;

import lombok.Data;
// import java.sql.Date; // <-- REMOVE THIS
import java.time.LocalDate; // <-- ADD THIS
import java.sql.Timestamp;

@Data
public class UserProfile {
    private Long id;
    private Long userId; // Foreign key to users.id
    private String firstName;
    private String lastName;
    private String userName;
    private String userProfile; // Avatar URL
    private LocalDate dob; // <-- CHANGE THIS
    private String address;
    private Timestamp createdAt;
    private Timestamp updatedAt;
}