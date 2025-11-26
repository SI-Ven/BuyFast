package com.example.buyfast.modules.product.dto;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class ProductResponse {
    private Long id;
    private UUID productUuid;
    private String productName;
    private String description;
    private Long categoryId;
    private boolean isActive;
    private List<VariantResponse> variants;

    @Data
    @Builder
    public static class VariantResponse {
        private UUID variantUuid;
        private BigDecimal price;
        private int stockQuantity;
        private String sku;
        private List<OptionResponse> options;
    }

    @Data
    @Builder
    public static class OptionResponse {
        private String optionName; // e.g., "Material"
        private String valueName;  // e.g., "Wooden"
        private List<String> images; // List of image URLs
    }
}