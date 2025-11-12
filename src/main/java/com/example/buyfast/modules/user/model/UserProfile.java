package com.example.buyfast.modules.user.model;

import lombok.Data;
import java.sql.Date;
import java.sql.Timestamp;

@Data
public class UserProfile {
    private Long id;
    private Long userId; // Foreign key to users.id
    private String firstName;
    private String lastName;
    private String userName;
    private String userProfile; // Avatar URL
    private Date dob;
    private String address;
    private Timestamp createdAt;
    private Timestamp updatedAt;
}