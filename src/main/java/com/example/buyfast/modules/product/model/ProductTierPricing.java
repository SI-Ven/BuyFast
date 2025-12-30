package com.example.buyfast.modules.product.model;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class ProductTierPricing {
    private Long id;
    private Long productVariantId;
    private Integer minQuantity; // e.g., 50
    private BigDecimal price;    // e.g., $9.00 (instead of $10.00)
}