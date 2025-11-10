package com.example.buyfast.modules.product.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import lombok.Data;
import java.math.BigDecimal;
import java.util.UUID;

@Data
public class UpdateProductRequest {

    // These fields are for the BASE product
    private String productName;
    private UUID categoryUuid;
    private String description;
    private Boolean isActive;

    // Note: Updating variants (price, stock) would be done
    // via a separate DTO and endpoint (e.g., PUT /api/v1/products/variants/{variantUuid})
    // For simplicity, we've omitted that from the ProductService for now.
}