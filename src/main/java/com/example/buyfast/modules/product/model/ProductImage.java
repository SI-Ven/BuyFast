package com.example.buyfast.modules.product.model;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class ProductImage {
    private Long id;
    private UUID imageUuid;
    private Long productId;
    private String imageUrl;
    private boolean isMain;
    private int sortOrder;
    private LocalDateTime createdAt;
    private Long variantId; // This links to a specific product_variant
}