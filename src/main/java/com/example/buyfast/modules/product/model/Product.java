package com.example.buyfast.modules.product.model;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Model class for the 'product' table.
 * Based on your Schema.sql
 */
@Data
public class Product {
    private Long id;
    private UUID productUuid;
    private String productName;
    private Long companyId;
    private Long sellerId;
    private Long categoryId;
    private String description;
    private BigDecimal price;
    private int stockQuantity;
    private boolean isActive; // Corresponds to is_active in the DB
    private LocalDateTime createdAt;
}