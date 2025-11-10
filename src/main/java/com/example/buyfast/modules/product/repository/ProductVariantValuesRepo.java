package com.example.buyfast.modules.product.repository;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ProductVariantValuesRepo {

    // This uses MyBatis's <foreach> to batch insert the links
    @Insert("<script>" +
            "INSERT INTO product_variant_values (variant_id, value_id) VALUES " +
            "<foreach item='valueId' collection='valueIds' separator=','>" +
            "(#{variantId}, #{valueId})" +
            "</foreach>" +
            "</script>")
    void linkVariantToValues(@Param("variantId") Long variantId, @Param("valueIds") List<Long> valueIds);
}