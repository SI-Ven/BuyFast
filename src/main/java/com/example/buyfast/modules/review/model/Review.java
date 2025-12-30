package com.example.buyfast.modules.review.model;

import lombok.Data;
import java.sql.Timestamp;
import java.util.UUID;

@Data
public class Review {
    private Long id;
    private UUID reviewUuid;
    private Long productId;
    private Long userId;
    private Integer rating; // 1-5
    private String reviewText;
    private Timestamp reviewDate;

    // For display purposes (joined data)
    private String userName;
    private String userAvatar;
}