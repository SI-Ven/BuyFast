package com.example.buyfast.modules.product.repository;

import com.example.buyfast.modules.product.model.ProductTierPricing;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import java.util.List;

@Mapper
public interface ProductTierPricingRepo {
    @Select("SELECT * FROM product_tier_pricing WHERE product_variant_id = #{variantId} ORDER BY min_quantity DESC")
    List<ProductTierPricing> findByVariantId(Long variantId);
}