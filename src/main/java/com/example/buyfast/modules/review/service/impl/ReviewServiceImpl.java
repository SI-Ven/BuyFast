package com.example.buyfast.modules.review.service.impl;

import com.example.buyfast.modules.product.model.Product;
import com.example.buyfast.modules.product.repository.ProductRepo;
import com.example.buyfast.modules.review.dto.CreateReviewRequest;
import com.example.buyfast.modules.review.model.Review;
import com.example.buyfast.modules.review.repository.ReviewRepo;
import com.example.buyfast.modules.review.service.ReviewService;
import com.example.buyfast.modules.user.model.User;
import com.example.buyfast.modules.user.repository.UserProfileRepo;
import com.example.buyfast.modules.user.repository.UserRepo;
import com.example.buyfast.util.UuidService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {
    private final ReviewRepo reviewRepo;
    private final ProductRepo productRepo;
    private final UserRepo userRepo;
    private final UuidService uuidService;
    private final UserProfileRepo userProfileRepo;
    @Override
    @Transactional
    public Review addReview(CreateReviewRequest request, UserDetails userDetails) {
        User user = userRepo.findByEmail(userDetails.getUsername())
                .orElseThrow(()->new RuntimeException("user not found"));

        Product product = productRepo.findByUuid(request.getProductUuid())
                .orElseThrow(()->new RuntimeException("product not found"));

        Review review = new Review();
        review.setReviewUuid(uuidService.generateUuid());
        review.setProductId(product.getId());
        review.setUserId(user.getId());
        review.setRating(request.getRating());
        review.setReviewText(request.getReviewText());

        review.setReviewDate(Timestamp.valueOf(LocalDateTime.now()));

        // --- FIX 2: Fetch & Set User Info (So name/avatar are not null) ---
        userProfileRepo.findByUserId(user.getId()).ifPresent(profile -> {
            review.setUserName(profile.getFirstName() + " " + profile.getLastName());
            review.setUserAvatar(profile.getUserProfile()); // Assuming this field holds the URL
        });

        reviewRepo.save(review);

        // update company reputation

        reviewRepo.updateCompanyRating(product.getId());

        return review;
    }

    @Override
    public List<Review> getProductReviews(UUID productUuid) {
        Product product = productRepo.findByUuid(productUuid)
                .orElseThrow(()->new RuntimeException("product not found"));

        return reviewRepo.findAllByProductId(product.getId());
    }

    @Transactional
    public void deleteReview(UUID reviewUuid, UserDetails userDetails) {
        User currentUser = userRepo.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new IllegalStateException("User not found"));

        // 1. Find the review
        Review review = reviewRepo.findByUuid(reviewUuid)
                .orElseThrow(() -> new IllegalStateException("Review not found"));

        // 2. Security Check: Only the author or an ADMIN can delete
        // (Assuming you might add admin logic later, for now we check ID)
        boolean isAdmin = currentUser.getRoles().stream()
                .anyMatch(r -> r.getRoleName().equals("super_admin")); // Optional: Allow admin

        if (!review.getUserId().equals(currentUser.getId()) && !isAdmin) {
            throw new SecurityException("You are not allowed to delete this review.");
        }

        // 3. Delete
        reviewRepo.deleteById(review.getId());

        // 4. CRITICAL: Recalculate the Company Rating
        // The review is gone, so the average will change.
        reviewRepo.updateCompanyRating(review.getProductId());
    }
}
