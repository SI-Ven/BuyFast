package com.example.buyfast.modules.product.dto;

import lombok.*;
import java.math.BigDecimal;
import java.util.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponse {
    private Long id;
    private UUID productUuid;
    private String productName;
    private String brandName;
    private String description;

    // Change sellerId (String) to seller (Object) to match Amazon/Alibaba style
    private SellerInfo seller;

    private String categoryName;
    private String mainCategoryName;

    @Builder.Default
    private Double averageRating = 0.0; // Ensures no null ratings
    @Builder.Default
    private Integer totalReviews = 0;

    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private String mainImage;
    private Map<String, Set<String>> availableOptions;
    private Long categoryId;
    private boolean isActive;
    private List<VariantResponse> variants;

    @Data
    @Builder
    public static class SellerInfo {
        private String storeName;
        private String email;
        private boolean isVerified;
    }
    @Data
    @Builder
    public static class VariantResponse {
        private UUID variantUuid;
        private BigDecimal price;
        private BigDecimal discountPercentage;
        private BigDecimal salePrice;
        private Integer stockQuantity;
        private String sku;
        private List<OptionResponse> options;
    }

    @Data
    @Builder
    public static class OptionResponse {
        private String optionName;
        private String valueName;
        private List<String> images;
    }
}