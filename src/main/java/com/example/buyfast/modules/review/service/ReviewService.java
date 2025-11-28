package com.example.buyfast.modules.review.service;

import com.example.buyfast.modules.review.dto.CreateReviewRequest;
import com.example.buyfast.modules.review.model.Review;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;
import java.util.UUID;

public interface ReviewService {
    Review addReview(CreateReviewRequest request, UserDetails userDetails);

    List<Review> getProductReviews(UUID productUuid);

    void deleteReview(UUID reviewUuid, UserDetails userDetails);
}
