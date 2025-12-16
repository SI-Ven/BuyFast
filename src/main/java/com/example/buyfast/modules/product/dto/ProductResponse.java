package com.example.buyfast.modules.product.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponse {
    private Long id;
    private UUID productUuid;
    private String productName;
    private String description;
    private String sellerId;

    // ✅ ADD THESE FIELDS
    private String categoryName;      // Subcategory Name (e.g. "Smartphones")
    private String mainCategoryName;  // Main Category Name (e.g. "Electronics")
    private Double averageRating;     // Average Rating
    // -------------------

    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private String mainImage;
    private Map<String, Set<String>> availableOptions;
    private Long categoryId;
    private boolean isActive;
    private List<VariantResponse> variants;

    // ... (Keep VariantResponse and OptionResponse classes as they are) ...
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
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
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OptionResponse {
        private String optionName;
        private String valueName;
        private List<String> images;
    }
}