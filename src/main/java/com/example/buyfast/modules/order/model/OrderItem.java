package com.example.buyfast.modules.order.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderItem {
    private Long id;
    private UUID itemUuid;
    private Long orderId;
    private Long productId;
    private Integer quantity;
    private BigDecimal pricePerUnit;
    private BigDecimal totalItemPrice;
}