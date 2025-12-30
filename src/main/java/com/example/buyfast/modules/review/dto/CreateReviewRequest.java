package com.example.buyfast.modules.review.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.UUID;

@Data
public class CreateReviewRequest {
    @NotNull
    private UUID productUuid;

    @Min(1) @Max(5)
    private Integer rating;

    @NotBlank
    private String reviewText;
}