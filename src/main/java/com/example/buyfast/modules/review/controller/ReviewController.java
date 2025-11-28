package com.example.buyfast.modules.review.controller;

import com.example.buyfast.common.ApiResponse;
import com.example.buyfast.modules.review.dto.CreateReviewRequest;
import com.example.buyfast.modules.review.model.Review;
import com.example.buyfast.modules.review.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping
    public ResponseEntity<ApiResponse<Review>> addReview(
            @Valid @RequestBody CreateReviewRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        Review review = reviewService.addReview(request, userDetails);
        return ResponseEntity.ok(ApiResponse.success("Review posted successfully.", review));
    }

    @GetMapping("/{productUuid}")
    public ResponseEntity<ApiResponse<List<Review>>> getProductReviews(@PathVariable UUID productUuid) {
        List<Review> reviews = reviewService.getProductReviews(productUuid);
        return ResponseEntity.ok(ApiResponse.success("Reviews retrieved.", reviews));
    }

    @DeleteMapping("/{reviewUuid}")
    public ResponseEntity<ApiResponse<Object>> deleteReview(
            @PathVariable UUID reviewUuid,
            @AuthenticationPrincipal UserDetails userDetails) {

        reviewService.deleteReview(reviewUuid, userDetails);
        return ResponseEntity.ok(ApiResponse.success("Review deleted successfully.", null));
    }
}