package com.example.buyfast.modules.favorite.repository;

import com.example.buyfast.config.mybatis.UuidTypeHandler;
import com.example.buyfast.modules.favorite.model.Favorite;
import com.example.buyfast.modules.product.dto.ProductResponse;
import com.example.buyfast.modules.product.dto.ProductResponse.VariantResponse;
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

    @Select("""
        SELECT 
            p.id, 
            p.product_uuid, 
            p.product_name, 
            p.description, 
            p.category_id, 
            p.is_active
        FROM favorite f
        JOIN product p ON f.product_id = p.id
        WHERE f.user_id = #{userId} AND p.is_active = true
        ORDER BY f.created_at DESC
    """)
    @Results(id = "ProductResponseMap", value = {
            @Result(property = "id", column = "id"),
            @Result(property = "productUuid", column = "product_uuid", typeHandler = UuidTypeHandler.class),
            @Result(property = "productName", column = "product_name"),
            @Result(property = "description", column = "description"),
            @Result(property = "categoryId", column = "category_id"),
            @Result(property = "isActive", column = "is_active"),

            // 2. FETCH IMAGE: Uses @One to call the helper method below
            @Result(property = "mainImage", column = "id",
                    one = @One(select = "selectMainImageByProductId")),

            // 3. FETCH VARIANTS: Uses @Many to populate the List<VariantResponse>
            @Result(property = "variants", column = "id",
                    many = @Many(select = "selectVariantsByProductId"))
    })
    List<ProductResponse> findFavoritesByUserId(Long userId);

    @Select("SELECT image_url FROM product_image WHERE product_id = #{productId} AND is_main = true LIMIT 1")
    String selectMainImageByProductId(Long productId);

    @Select("""
        SELECT 
            variant_uuid, 
            price, 
            stock_quantity, 
            sku 
        FROM product_variant 
        WHERE product_id = #{productId}
    """)
    @Results({
            @Result(property = "variantUuid", column = "variant_uuid", typeHandler = UuidTypeHandler.class)
    })
    List<VariantResponse> selectVariantsByProductId(Long productId);

    @Delete("""
DELETE FROM favorite WHERE user_id = #{userId} AND product_id = #{productId}
""")
    void deleteFavorite(@Param("userId")Long userId, @Param("productId")Long productId);
}
