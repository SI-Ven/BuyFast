package com.example.buyfast.modules.product.model;

import lombok.Data;
import java.util.UUID;

@Data
public class ProductOptionValue {
    private Long id;
    private UUID valueUuid;
    private Long optionId;
    private String valueName;
}