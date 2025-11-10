package com.example.buyfast.modules.product.repository;

import com.example.buyfast.modules.product.model.ProductOptionValue;
import org.apache.ibatis.annotations.*;

import java.util.Optional;

@Mapper
public interface ProductOptionValueRepo {

    @Insert("INSERT INTO product_option_value (value_uuid, option_id, value_name) " +
            "VALUES (#{valueUuid}, #{optionId}, #{valueName})")
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    void insert(ProductOptionValue value);

    @Select("SELECT * FROM product_option_value WHERE option_id = #{optionId} AND value_name = #{valueName}")
    Optional<ProductOptionValue> findByOptionIdAndName(@Param("optionId") Long optionId, @Param("valueName") String valueName);
}