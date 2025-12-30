package com.example.buyfast.modules.product.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
public class OptionValueRequest {
    @NotBlank(message = "Option name is required (e.g., 'Size')")
    private String optionName; // "Size"

    @NotBlank(message = "Value name is required (e.g., 'Small')")
    private String valueName;  // "Small"
    private List<String> imgUrls;
}