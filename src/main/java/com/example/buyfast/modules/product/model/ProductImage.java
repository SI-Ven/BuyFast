// src/main/java/com/example/buyfast/modules/product/model/ProductImage.java
package com.example.buyfast.modules.product.model;

import lombok.Data;
import java.sql.Timestamp;
import java.util.UUID;

@Data
public class ProductImage {
    private Long id;
    private UUID imageUuid;
    private Long productId;
    private String imageUrl;
    private Boolean isMain;
    private Integer sortOrder;
    private Timestamp createdAt;

    private Long variantId;

    // --- NEW FIELD ---
    private Long optionValueId; // Maps to database column 'option_value_id'
}