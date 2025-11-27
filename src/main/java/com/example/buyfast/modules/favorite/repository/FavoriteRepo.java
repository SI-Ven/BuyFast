package com.example.buyfast.modules.favorite.repository;

import com.example.buyfast.config.mybatis.UuidTypeHandler;
import com.example.buyfast.modules.favorite.model.Favorite;
import com.example.buyfast.modules.product.dto.ProductResponse;
import org.apache.ibatis.annotations.*;
import org.apache.ibatis.type.JdbcType;

import java.util.List;

@Mapper
public interface FavoriteRepo {

    @Select("""
        SELECT COUNT(*) > 0 FROM favorite WHERE user_id = #{userId} AND product_id = #{productId}
""")
    boolean existsByUserAndProduct( @Param("userId")Long userId,@Param("productId")Long productId);

    @Select("""
        INSERT INTO favorite(favorite_uuid, user_id, product_id, created_at)
        VALUES (#{favoriteUuid}, #{userId}, #{productId}, CURRENT_TIMESTAMP)
        RETURNING *
    """)
    @Results(id = "favoriteMap", value = {
            @Result(property = "id", column = "id"),
            @Result(property = "favoriteUuid", column = "favorite_uuid", typeHandler = UuidTypeHandler.class),
            @Result(property = "userId", column = "user_id"),
            @Result(property = "productId", column = "product_id"),
            @Result(property = "createdAt", column = "created_at", jdbcType = JdbcType.TIMESTAMP)
    })
    Favorite save(Favorite favorite);

    @Select("SELECT " +
            "p.id, p.product_uuid, p.product_name, p.description, p.category_id, p.is_active, " +
            "MIN(pv.price) as minPrice, " +
            "MAX(pv.price) as maxPrice, " +
            "(SELECT pi.image_url FROM product_image pi WHERE pi.product_id = p.id AND pi.is_main = true LIMIT 1) as mainImage " +
            "FROM favorite f " +
            "JOIN product p ON f.product_id = p.id " +
            "LEFT JOIN product_variant pv ON p.id = pv.product_id " +
            "WHERE f.user_id = #{userId} AND p.is_active = true " +
            "GROUP BY p.id, p.product_uuid, p.product_name, p.description, p.category_id, p.is_active, p.created_at " +
            "ORDER BY f.created_at DESC")
    @Results({
            @Result(property = "productUuid", column = "product_uuid"),
            @Result(property = "productName", column = "product_name"),
            @Result(property = "categoryId", column = "category_id"),
            @Result(property = "minPrice", column = "minPrice"),
            @Result(property = "maxPrice", column = "maxPrice"),
            @Result(property = "mainImage", column = "mainImage")
    })
    List<ProductResponse> findFavoritesByUserId(Long userId);
}
