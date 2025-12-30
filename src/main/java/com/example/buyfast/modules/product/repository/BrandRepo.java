package com.example.buyfast.modules.product.repository;

import com.example.buyfast.modules.product.model.Brand;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;

import java.util.Optional;
import java.util.UUID;
import java.util.List;

@Mapper
public interface BrandRepo {
    // ... existing methods ...
    @Select("SELECT * FROM brand WHERE brand_uuid = #{brandUuid}")
    Optional<Brand> findByUuid(UUID brandUuid);

    @Select("SELECT * FROM brand WHERE id = #{id}")
    Optional<Brand> findById(Long id); // Add this method

    @Select("SELECT * FROM brand")
    List<Brand> findAll();

    // ✅ ADD THESE TWO METHODS:
    @Select("SELECT * FROM brand WHERE brand_name = #{brandName}")
    Optional<Brand> findByName(String brandName);

    @Insert("INSERT INTO brand (brand_uuid, brand_name, description, logo_url, created_at) " +
            "VALUES (#{brandUuid}, #{brandName}, #{description}, #{logoUrl}, CURRENT_TIMESTAMP)")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void save(Brand brand);

}