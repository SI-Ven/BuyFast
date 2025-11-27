package com.example.buyfast.modules.order.service.impl;

import com.example.buyfast.modules.address.model.ShippingAddress;
import com.example.buyfast.modules.address.repository.AddressRepo;
import com.example.buyfast.modules.order.dto.CreateOrderRequest;
import com.example.buyfast.modules.order.model.Order;
import com.example.buyfast.modules.order.model.OrderItem;
import com.example.buyfast.modules.order.repository.OrderItemRepo;
import com.example.buyfast.modules.order.repository.OrderRepo;
import com.example.buyfast.modules.order.service.OrderService;
import com.example.buyfast.modules.product.model.ProductVariant;
import com.example.buyfast.modules.product.repository.ProductVariantRepo;
import com.example.buyfast.modules.user.model.User;
import com.example.buyfast.modules.user.repository.UserRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepo orderRepo;
    private final OrderItemRepo orderItemRepo;
    private final AddressRepo addressRepo;
    private final UserRepo userRepo;
    private final ProductVariantRepo productVariantRepo;

    @Override
    @Transactional
    public Order createOrder(CreateOrderRequest request, UserDetails userDetails) {
        // 1. Validate User
        User user = userRepo.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // 2. Fetch Address Snapshot
        ShippingAddress address = addressRepo.findByUuidAndUser(request.getAddressUuid(), user.getId())
                .orElseThrow(() -> new IllegalArgumentException("Shipping address not found"));

        // 3. PRE-CALCULATE Total Price & Prepare Items
        // We do this BEFORE saving the order so the DB gets the correct total immediately.
        BigDecimal finalTotal = BigDecimal.ZERO;
        List<OrderItem> itemsToSave = new ArrayList<>();

        for (CreateOrderRequest.OrderItemRequest itemReq : request.getItems()) {
            ProductVariant variant = productVariantRepo.findById(itemReq.getProductVariantId())
                    .orElseThrow(() -> new IllegalArgumentException("Product variant not found: " + itemReq.getProductVariantId()));

            // Check Stock (Optional but recommended)
            if (variant.getStockQuantity() < itemReq.getQuantity()) {
                throw new IllegalArgumentException("Insufficient stock for SKU: " + variant.getSku());
            }

            BigDecimal lineItemTotal = variant.getPrice().multiply(BigDecimal.valueOf(itemReq.getQuantity()));
            finalTotal = finalTotal.add(lineItemTotal);

            // Prepare the item object (without order ID yet)
            itemsToSave.add(OrderItem.builder()
                    .itemUuid(UUID.randomUUID())
                    .productId(variant.getProductId())
                    .quantity(itemReq.getQuantity())
                    .pricePerUnit(variant.getPrice()) // Snapshot price
                    .totalItemPrice(lineItemTotal)
                    .build());
        }

        // 4. Create and Save Order (With Correct Price)
        Order order = Order.builder()
                .orderUuid(UUID.randomUUID())
                .userId(user.getId())
                // --- SNAPSHOT ADDRESS ---
                .shippingFullName(address.getFullName())
                .shippingAddressLine1(address.getAddressLine1())
                .shippingCity(address.getCity())
                .shippingCountry(address.getCountry())
                .shippingPhone(address.getPhoneNumber())
                .originalShippingAddressId(address.getId())
                // ------------------------
                .status("pending")
                .paymentMethod(request.getPaymentMethod())
                .totalPrice(finalTotal) // ✅ Correct Price
                .createdAt(LocalDateTime.now()) // <--- SET THIS
                .updatedAt(LocalDateTime.now())
                .build();

        orderRepo.save(order);

        // 5. Save Order Items (Now that we have Order ID)
        for (OrderItem item : itemsToSave) {
            item.setOrderId(order.getId());
            orderItemRepo.save(item);
        }

        return order;
    }

    @Override
    public List<Order> getMyOrders(UserDetails userDetails) {
        User user = userRepo.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return orderRepo.findAllByUserId(user.getId());
    }

    @Override
    @Transactional
    public void cancelOrder(UUID orderUuid, UserDetails userDetails) {
        User user = userRepo.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Order order = orderRepo.findByUuid(orderUuid)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));

        // SECURITY: User can only cancel their OWN order
        if (!order.getUserId().equals(user.getId())) {
            throw new SecurityException("Access denied: You cannot cancel this order.");
        }

        // LOGIC: Can only cancel if currently 'pending'
        if (!"pending".equalsIgnoreCase(order.getStatus())) {
            throw new IllegalStateException("Cannot cancel order. Current status is: " + order.getStatus());
        }

        orderRepo.updateStatus(order.getId(), "cancelled");
    }

    // ==========================================================
    // 2️⃣ ADMIN: UPDATE STATUS (Shipped, Delivered, etc.)
    // ==========================================================
    @Override
    @Transactional
    public void updateOrderStatus(UUID orderUuid, String newStatus) {
        Order order = orderRepo.findByUuid(orderUuid)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));

        // Validate Status (Optional, but good practice)
        // List<String> validStatuses = Arrays.asList("pending", "shipped", "delivered", "cancelled", "rejected");
        // if (!validStatuses.contains(newStatus.toLowerCase())) { ... }

        orderRepo.updateStatus(order.getId(), newStatus);
    }
}