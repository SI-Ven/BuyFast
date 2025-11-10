package com.example.buyfast.modules.product.repository;

import com.example.buyfast.modules.product.model.ProductOption;
import org.apache.ibatis.annotations.*;

import java.util.Optional;

@Mapper
public interface ProductOptionRepo {

    @Insert("INSERT INTO product_option (option_uuid, product_id, option_name) " +
            "VALUES (#{optionUuid}, #{productId}, #{optionName})")
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    void insert(ProductOption option);

    @Select("SELECT * FROM product_option WHERE product_id = #{productId} AND option_name = #{optionName}")
    Optional<ProductOption> findByProductIdAndName(@Param("productId") Long productId, @Param("optionName") String optionName);
}