package com.example.buyfast.modules.product.repository;

import com.example.buyfast.modules.product.model.ProductOptionValue;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ProductVariantValuesRepo {


    // --- NEW METHOD FOR SINGLE INSERT ---
    @Insert("INSERT INTO product_variant_values (variant_id, value_id) VALUES (#{variantId}, #{valueId})")
    void insert(@Param("variantId") Long variantId, @Param("valueId") Long valueId);

    @Select("SELECT pov.*, po.option_name as tempOptionName " +
            "FROM product_variant_values pvv " +
            "JOIN product_option_value pov ON pvv.value_id = pov.id " +
            "JOIN product_option po ON pov.option_id = po.id " +
            "WHERE pvv.variant_id = #{variantId}")
    List<ProductOptionValue> findValuesByVariantId(@Param("variantId") Long variantId);
}