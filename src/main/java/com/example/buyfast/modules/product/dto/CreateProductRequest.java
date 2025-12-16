package com.example.buyfast.modules.product.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.util.List;
import java.util.UUID;

@Data
public class CreateProductRequest {

    @NotBlank(message = "Product name is required")
    private String productName;

    @NotNull(message = "Category is required")
    private UUID categoryUuid;

    // Removed @NotNull annotation. Logic is handled in Service.
    private UUID brandUuid;

    // New field for custom brand input
    private String newBrandName;

    @NotBlank(message = "Description is required")
    private String description;

    @Valid
    @NotNull
    @Size(min = 1, message = "Product must have at least one variant")
    private List<VariantRequest> variants;
}