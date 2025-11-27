package com.example.buyfast.modules.order.service;

import com.example.buyfast.modules.order.dto.CreateOrderRequest;
import com.example.buyfast.modules.order.model.Order;
import org.springframework.security.core.userdetails.UserDetails;
import java.util.List;
import java.util.UUID;

public interface OrderService {
    Order createOrder(CreateOrderRequest request, UserDetails userDetails);
    List<Order> getMyOrders(UserDetails userDetails);
    void cancelOrder(UUID orderUuid, UserDetails userDetails);

    // NEW: Admin updates status (Shipped, Delivered, etc.)
    void updateOrderStatus(UUID orderUuid, String newStatus);
}