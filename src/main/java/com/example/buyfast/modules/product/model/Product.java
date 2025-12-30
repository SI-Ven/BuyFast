package com.example.buyfast.modules.product.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class Product {
    private Long id;
    private UUID productUuid;
    private String productName;
    private Long companyId;
    private Long sellerId;
    private Long categoryId;
    private Long brandId; // <--- NEW FIELD
    private String description;
    private boolean isActive;
    private LocalDateTime createdAt;

    public boolean isActive() {
        return isActive;
    }
}