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
}