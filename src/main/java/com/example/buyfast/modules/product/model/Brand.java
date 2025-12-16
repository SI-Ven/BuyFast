package com.example.buyfast.modules.product.model;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class Brand {
    private Long id;
    private UUID brandUuid;
    private String brandName;
    private String logoUrl;
    private String description;
    private LocalDateTime createdAt;
}