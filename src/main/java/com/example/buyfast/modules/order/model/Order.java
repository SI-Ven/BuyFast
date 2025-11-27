package com.example.buyfast.modules.order.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Order {
    private Long id;
    private UUID orderUuid;
    private Long userId;

    // Snapshot Fields (The "Truth" for this order)
    private String shippingFullName;
    private String shippingAddressLine1;
    private String shippingCity;
    private String shippingCountry;
    private String shippingPhone;

    private Long originalShippingAddressId; // Optional reference

    private BigDecimal totalPrice;
    private String status; // pending, shipped, etc.
    private String paymentMethod;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}