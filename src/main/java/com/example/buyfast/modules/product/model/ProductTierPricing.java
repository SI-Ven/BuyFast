package com.example.buyfast.modules.product.model;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class ProductTierPricing {
    private Long id;
    private Long productVariantId;
    private Integer minQuantity;
    private BigDecimal price;
}