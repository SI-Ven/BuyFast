package com.example.buyfast.modules.category.model;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class Category {

    private Long id; // Internal ID (BIGSERIAL)
    private UUID categoryUuid; // External ID (UUID)
    private String categoryName;
    private String description;
    private String iconUrl;
    private Long mainCategoryId; // Internal FK to main_category(id)
    private Integer level;
    private LocalDateTime createdAt;

}