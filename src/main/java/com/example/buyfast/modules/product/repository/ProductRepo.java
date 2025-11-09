package com.example.buyfast.modules.product.repository;

import com.example.buyfast.modules.product.model.Product;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Mapper
public interface ProductRepo {

    /**
     * Finds a product by its public UUID.
     */
    @Select("SELECT * FROM product WHERE product_uuid = #{uuid}")
    Optional<Product> findByUuid(UUID uuid);

    /**
     * Gets a list of all products in the system.
     */
    @Select("SELECT * FROM product ORDER BY created_at DESC")
    List<Product> findAllProducts();

    /**
     * Allows a Super Admin to activate or deactivate a product.
     */
    @Update("UPDATE product SET is_active = #{isActive} WHERE product_uuid = #{uuid}")
    void updateProductActiveStatus(@Param("uuid") UUID uuid, @Param("isActive") boolean isActive);
}