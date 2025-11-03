package com.example.buyfast.modules.category.dto;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateMainCategoryRequest {

    @NotBlank(message = "Category name is required")
    @Size(max = 100)
    private String mainCategoryName;

    private String description;

    @Size(max = 512)
    private String iconUrl;
}