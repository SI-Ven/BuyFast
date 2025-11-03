package com.example.buyfast.modules.category.dto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.util.UUID;

@Data
public class CreateCategoryRequest {

    @NotBlank(message = "Category name is required")
    @Size(max = 100)
    private String categoryName;

    @NotNull(message = "Parent mainCategoryUuid is required")
    private UUID mainCategoryUuid; // The public UUID of the parent

    private String description;

    @Size(max = 512)
    private String iconUrl;
}