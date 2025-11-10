package com.example.buyfast.modules.product.repository;

import com.example.buyfast.modules.product.model.ProductVariant;
import org.apache.ibatis.annotations.*;

import java.util.Optional;
import java.util.UUID;

@Mapper
public interface ProductVariantRepo {

    @Insert("INSERT INTO product_variant (variant_uuid, product_id, sku, price, stock_quantity, is_active, created_at) " +
            "VALUES (#{variantUuid}, #{productId}, #{sku}, #{price}, #{stockQuantity}, #{isActive}, CURRENT_TIMESTAMP)")
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    void insert(ProductVariant variant);

    @Select("SELECT * FROM product_variant WHERE variant_uuid = #{variantUuid}")
    Optional<ProductVariant> findByUuid(UUID variantUuid);
}