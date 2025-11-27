package com.example.buyfast.modules.order.controller;

import com.example.buyfast.common.ApiResponse;
import com.example.buyfast.modules.order.dto.CreateOrderRequest;
import com.example.buyfast.modules.order.model.Order;
import com.example.buyfast.modules.order.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    @Operation(summary = "Checkout / Place a new order")
    public ResponseEntity<ApiResponse<Order>> createOrder(
            @Valid @RequestBody CreateOrderRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        Order order = orderService.createOrder(request, userDetails);
        return ResponseEntity.ok(ApiResponse.success("Order placed successfully", order));
    }

    @GetMapping
    @Operation(summary = "Get my order history")
    public ResponseEntity<ApiResponse<List<Order>>> getMyOrders(
            @AuthenticationPrincipal UserDetails userDetails) {

        return ResponseEntity.ok(ApiResponse.success("Orders retrieved", orderService.getMyOrders(userDetails)));
    }
    @PutMapping("/{orderUuid}/status")
    @Operation(summary = "Update order status (Shipped, Delivered, etc.)")
    public ResponseEntity<ApiResponse<Void>> updateOrderStatus(
            @PathVariable UUID orderUuid,
            @RequestParam String status) { // Or use a @RequestBody DTO if you prefer

        orderService.updateOrderStatus(orderUuid, status);
        return ResponseEntity.ok(ApiResponse.success("Order status updated to " + status, null));
    }

    @PutMapping("/{orderUuid}/cancel")
    @Operation(summary = "Cancel my order")
    public ResponseEntity<ApiResponse<Void>> cancelOrder(
            @PathVariable UUID orderUuid,
            @AuthenticationPrincipal UserDetails userDetails) {

        orderService.cancelOrder(orderUuid, userDetails);
        return ResponseEntity.ok(ApiResponse.success("Order cancelled successfully", null));
    }
}