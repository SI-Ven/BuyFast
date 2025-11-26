package com.example.buyfast.modules.product.repository;

import com.example.buyfast.modules.product.model.ProductImage;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Mapper
public interface ProductImageRepo {

    @Insert("INSERT INTO product_image (image_uuid, product_id, image_url, is_main, sort_order, created_at, variant_id, option_value_id) " +
            "VALUES (#{imageUuid}, #{productId}, #{imageUrl}, #{isMain}, #{sortOrder}, CURRENT_TIMESTAMP, #{variantId}, #{optionValueId})")
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    void insert(ProductImage image);

    @Select("SELECT * FROM product_image WHERE image_uuid = #{imageUuid}")
    Optional<ProductImage> findByUuid(UUID imageUuid);

    @Delete("DELETE FROM product_image WHERE image_uuid = #{imageUuid}")
    void deleteByUuid(UUID imageUuid);

    @Select("SELECT COUNT(*) > 0 FROM product_image WHERE image_url = #{url} AND option_value_id = #{valueId}")
    boolean existsByUrlAndValueId(@Param("url") String url, @Param("valueId") Long valueId);

    @Select("SELECT * FROM product_image WHERE option_value_id = #{valueId}")
    List<ProductImage> findAllByOptionValueId(@Param("valueId") Long valueId);

    @Select("SELECT * FROM product_image WHERE product_id = #{productId}")
    List<ProductImage> findAllByProductId(@Param("productId") Long productId);

    // --- NEW METHOD FOR UPDATE LOGIC ---
    @Delete("DELETE FROM product_image WHERE product_id = #{productId}")
    void deleteAllByProductId(@Param("productId") Long productId);
}