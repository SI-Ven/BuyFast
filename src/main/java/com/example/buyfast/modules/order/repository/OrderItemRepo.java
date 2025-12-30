package com.example.buyfast.modules.order.repository;

import com.example.buyfast.config.mybatis.UuidTypeHandler;
import com.example.buyfast.modules.order.model.OrderItem;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;

@Mapper
public interface OrderItemRepo {

    @Insert("""
        INSERT INTO order_items (
            item_uuid, order_id, product_id, quantity, price_per_unit, total_item_price
        ) VALUES (
            #{itemUuid, typeHandler=com.example.buyfast.config.mybatis.UuidTypeHandler},
            #{orderId}, #{productId}, #{quantity}, #{pricePerUnit}, #{totalItemPrice}
        )
    """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void save(OrderItem item);
}