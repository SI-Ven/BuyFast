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

    // Hints for card display
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private String mainImage;

    private Map<String, Set<String>> availableOptions;

    private Long categoryId;

    private boolean isActive;

    private List<VariantResponse> variants;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VariantResponse {
        private UUID variantUuid;
        private BigDecimal price;

        // ✅ ADD THESE TWO FIELDS
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