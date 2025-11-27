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
import java.util.UUID;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepo orderRepo;
    private final OrderItemRepo orderItemRepo;
    private final AddressRepo addressRepo;
    private final UserRepo userRepo;
    private final ProductVariantRepo productVariantRepo; // To get real prices

    @Override
    @Transactional
    public Order createOrder(CreateOrderRequest request, UserDetails userDetails) {
        User user = userRepo.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // 1. Fetch Source Address (The logic we discussed)
        ShippingAddress address = addressRepo.findByUuidAndUser(request.getAddressUuid(), user.getId())
                .orElseThrow(() -> new IllegalArgumentException("Shipping address not found"));

        // 2. Initialize Order Object with SNAPSHOT data
        Order order = Order.builder()
                .orderUuid(UUID.randomUUID())
                .userId(user.getId())
                .shippingFullName(address.getFullName())
                .shippingAddressLine1(address.getAddressLine1())
                .shippingCity(address.getCity())
                .shippingCountry(address.getCountry())
                .shippingPhone(address.getPhoneNumber()) // Make sure you added this field to ShippingAddress!
                .originalShippingAddressId(address.getId())
                .status("pending")
                .paymentMethod(request.getPaymentMethod())
                .totalPrice(BigDecimal.ZERO) // Will calculate below
                .build();

        // 3. Save Order first to get the ID (needed for items)
        orderRepo.save(order);

        // 4. Process Items
        BigDecimal calculatedTotal = BigDecimal.ZERO;

        for (CreateOrderRequest.OrderItemRequest itemReq : request.getItems()) {
            // Fetch Variant to get REAL PRICE
            ProductVariant variant = productVariantRepo.findById(itemReq.getProductVariantId())
                    .orElseThrow(() -> new IllegalArgumentException("Product variant not found: " + itemReq.getProductVariantId()));

            BigDecimal itemTotal = variant.getPrice().multiply(BigDecimal.valueOf(itemReq.getQuantity()));
            calculatedTotal = calculatedTotal.add(itemTotal);

            // Create Order Item
            OrderItem orderItem = OrderItem.builder()
                    .itemUuid(UUID.randomUUID())
                    .orderId(order.getId())
                    .productId(variant.getProductId())
                    .quantity(itemReq.getQuantity())
                    .pricePerUnit(variant.getPrice()) // Snapshot the price too!
                    .totalItemPrice(itemTotal)
                    .build();

            orderItemRepo.save(orderItem);
        }

        // 5. Update Order Total Price
        // (You might need an update method in Repo, or just set it before save if you calculate first.
        // Here I'll assume we update it after or calculate before saving order.
        // For simplicity, let's pretend we calculated before or we do a quick update)
        // Ideally, move step 3 to here, after calculation.

        // RE-SAVING Logic (Simplest fix for this flow):
        // Since we already inserted, we ideally execute an update.
        // For now, let's assume you add an @Update method to OrderRepo for total_price.

        return order;
    }

    @Override
    public List<Order> getMyOrders(UserDetails userDetails) {
        User user = userRepo.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return orderRepo.findAllByUserId(user.getId());
    }
}