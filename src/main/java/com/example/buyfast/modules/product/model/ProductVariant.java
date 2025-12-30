package com.example.buyfast.modules.product.model;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class ProductVariant {
    private Long id;
    private UUID variantUuid;
    private Long productId;
    private String sku;
    private BigDecimal price;
    private int stockQuantity;
    private BigDecimal discountPercentage;
    private boolean isActive;
    private LocalDateTime createdAt;
}