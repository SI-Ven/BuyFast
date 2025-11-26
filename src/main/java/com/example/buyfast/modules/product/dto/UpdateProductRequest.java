package com.example.buyfast.modules.product.dto;

import jakarta.validation.Valid;
import lombok.Data;
import java.util.List;
import java.util.UUID;

@Data
public class UpdateProductRequest {

    // Base product fields
    private String productName;
    private UUID categoryUuid;
    private String description;
    private Boolean isActive;

    // New: Allow updating variants (Full replacement strategy)
    @Valid
    private List<VariantRequest> variants;
}