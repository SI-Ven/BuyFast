package com.example.buyfast.modules.product.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class VariantRequest {
    @NotNull(message = "Price is required for each variant")
    @DecimalMin(value = "0.01", message = "Price must be greater than 0")
    private BigDecimal price;

    @NotNull(message = "Stock quantity is required for each variant")
    @Min(value = 0, message = "Stock cannot be negative")
    private Integer stockQuantity;

    @DecimalMin(value = "0.00", message = "Discount cannot be negative")
    private BigDecimal discountPercentage;

    @Valid
    @NotNull
    @Size(min = 1, message = "Each variant must have at least one option")
    // Example: [{"optionName": "Size", "valueName": "Small"}, {"optionName": "Color", "valueName": "Red"}]
    private List<OptionValueRequest> options;
}