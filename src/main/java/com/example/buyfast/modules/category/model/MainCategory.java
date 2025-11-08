package com.example.buyfast.modules.category.model;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List; // <-- NEW IMPORT
import java.util.UUID;

@Data
public class MainCategory {
    private Long id;
    private UUID mainCategoryUuid;
    private String mainCategoryName;
    private String description;
    private String iconUrl;
    private String status;
    private LocalDateTime createdAt;

    // --- NEW FIELD ---
    // This list will be populated by MyBatis
    private List<Category> subCategories;
}