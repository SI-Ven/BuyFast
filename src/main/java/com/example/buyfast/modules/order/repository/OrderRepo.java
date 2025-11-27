package com.example.buyfast.modules.order.repository;

import com.example.buyfast.config.mybatis.UuidTypeHandler;
import com.example.buyfast.modules.order.model.Order;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.UUID;

@Mapper
public interface OrderRepo {

    @Insert("""
        INSERT INTO orders (
            order_uuid, user_id, 
            shipping_full_name, shipping_address_line_1, shipping_city, shipping_country, shipping_phone, 
            original_shipping_address_id,
            total_price, status, payment_method, created_at, updated_at
        ) VALUES (
            #{orderUuid, typeHandler=com.example.buyfast.config.mybatis.UuidTypeHandler}, 
            #{userId},
            #{shippingFullName}, #{shippingAddressLine1}, #{shippingCity}, #{shippingCountry}, #{shippingPhone}, 
            #{originalShippingAddressId},
            #{totalPrice}, #{status}, #{paymentMethod}, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
        )
    """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void save(Order order);

    @Select("SELECT * FROM orders WHERE user_id = #{userId} ORDER BY created_at DESC")
    @Results({
            @Result(property = "orderUuid", column = "order_uuid", typeHandler = UuidTypeHandler.class),
            // Map other fields automatically if names match
    })
    List<Order> findAllByUserId(Long userId);
}