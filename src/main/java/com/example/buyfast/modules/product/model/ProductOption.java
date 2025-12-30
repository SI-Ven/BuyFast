package com.example.buyfast.modules.product.model;

import lombok.Data;
import java.util.UUID;

@Data
public class ProductOption {
    private Long id;
    private UUID optionUuid;
    private Long productId;
    private String optionName;
}