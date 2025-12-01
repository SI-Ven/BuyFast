package com.example.buyfast.modules.rfq.service;

import com.example.buyfast.modules.user.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class RfqService {

    private final JdbcTemplate jdbcTemplate; // Using JDBC for speed/simplicity here

    public void submitRfq(Long userId, String productName, int quantity, BigDecimal targetPrice) {
        String sql = "INSERT INTO rfq_request (user_id, product_name, quantity_required, target_price) VALUES (?, ?, ?, ?)";
        jdbcTemplate.update(sql, userId, productName, quantity, targetPrice);
    }

    public void submitBid(Long rfqId, Long sellerId, BigDecimal price, String message) {
        String sql = "INSERT INTO rfq_bid (rfq_id, seller_id, price, message) VALUES (?, ?, ?, ?)";
        jdbcTemplate.update(sql, rfqId, sellerId, price, message);
    }
}