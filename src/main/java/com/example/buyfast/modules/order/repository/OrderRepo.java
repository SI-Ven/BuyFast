package com.example.buyfast.modules.order.repository;

import com.example.buyfast.config.mybatis.UuidTypeHandler;
import com.example.buyfast.modules.order.model.Order;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Optional;
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

    // 1. DEFINE the "OrderMap" here using 'id'
    @Select("SELECT * FROM orders WHERE user_id = #{userId} ORDER BY created_at DESC")
    @Results(id = "OrderMap", value = {
            @Result(property = "id", column = "id"),
            @Result(property = "orderUuid", column = "order_uuid", typeHandler = UuidTypeHandler.class),
            @Result(property = "userId", column = "user_id"),
            @Result(property = "shippingFullName", column = "shipping_full_name"),
            @Result(property = "shippingAddressLine1", column = "shipping_address_line_1"),
            @Result(property = "shippingCity", column = "shipping_city"),
            @Result(property = "shippingCountry", column = "shipping_country"),
            @Result(property = "shippingPhone", column = "shipping_phone"),
            @Result(property = "originalShippingAddressId", column = "original_shipping_address_id"),
            @Result(property = "totalPrice", column = "total_price"),
            @Result(property = "status", column = "status"),
            @Result(property = "paymentMethod", column = "payment_method"),
            @Result(property = "createdAt", column = "created_at"),
            @Result(property = "updatedAt", column = "updated_at")
    })
    List<Order> findAllByUserId(Long userId);

    // 2. REUSE "OrderMap" here
    @Select("SELECT * FROM orders WHERE order_uuid = #{orderUuid, typeHandler=com.example.buyfast.config.mybatis.UuidTypeHandler}")
    @ResultMap("OrderMap")
    Optional<Order> findByUuid(UUID orderUuid);

    @Update("UPDATE orders SET status = #{status}, updated_at = CURRENT_TIMESTAMP WHERE id = #{id}")
    void updateStatus(@Param("id") Long id, @Param("status") String status);

    @Select("SELECT * FROM orders WHERE id = #{id}")
    @ResultMap("OrderMap")
    Optional<Order> findById(Long id);
}