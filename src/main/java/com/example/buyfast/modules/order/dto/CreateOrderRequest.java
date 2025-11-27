package com.example.buyfast.modules.order.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class CreateOrderRequest {

    @NotNull(message = "Shipping address is required")
    private UUID addressUuid; // User selects an ID from their address book

    @NotEmpty(message = "Order must have items")
    private List<OrderItemRequest> items;

    @NotNull(message = "Payment method is required")
    private String paymentMethod;

    @Data
    public static class OrderItemRequest {
        @NotNull
        private Long productVariantId; // We buy a specific variant (Size/Color)

        @NotNull
        private Integer quantity;
    }
}