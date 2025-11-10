package com.example.buyfast.modules.product.model;

import lombok.Data;
// import java.math.BigDecimal; // <-- REMOVED
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class Product {
    private Long id;
    private UUID productUuid;
    private String productName;
    private Long companyId;
    private Long sellerId;
    private Long categoryId;
    private String description;
    // REMOVED: private BigDecimal price;
    // REMOVED: private int stockQuantity;
    private boolean isActive;
    private LocalDateTime createdAt;
}