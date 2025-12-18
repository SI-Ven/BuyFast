package com.example.buyfast.modules.product.repository;

import com.example.buyfast.modules.product.dto.ProductResponse;
import com.example.buyfast.modules.product.model.Product;
import org.apache.ibatis.annotations.*;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Mapper
public interface ProductRepo {

    // --- ✅ FIXED: Added brand_id to INSERT statement ---
    @Insert("INSERT INTO product (product_uuid, product_name, company_id, seller_id, category_id, brand_id, " +
            "description, is_active, created_at) " +
            "VALUES (#{productUuid}, #{productName}, #{companyId}, #{sellerId}, #{categoryId}, #{brandId}, " +
            "#{description}, #{isActive}, CURRENT_TIMESTAMP)")
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    void insert(Product product);

    @Select("SELECT * FROM product WHERE product_uuid = #{productUuid} AND seller_id = #{sellerId}")
    Optional<Product> findByUuidAndSellerId(@Param("productUuid") UUID productUuid, @Param("sellerId") Long sellerId);

    @Select("SELECT * FROM product WHERE seller_id = #{sellerId} ORDER BY created_at DESC")
    List<Product> findAllBySellerId(Long sellerId);

    @Delete("DELETE FROM product WHERE product_uuid = #{productUuid} AND seller_id = #{sellerId}")
    void deleteByUuidAndSellerId(@Param("productUuid") UUID productUuid, @Param("sellerId") Long sellerId);

    @Select("SELECT * FROM product WHERE id = #{id}")
    Optional<Product> findById(Long id);

    @Select("SELECT * FROM product WHERE product_uuid = #{uuid}")
    Optional<Product> findByUuid(UUID uuid);

    @Select("SELECT * FROM product ORDER BY created_at DESC")
    List<Product> findAllProducts();

    @Update("UPDATE product SET is_active = #{isActive} WHERE product_uuid = #{uuid}")
    void updateProductActiveStatus(@Param("uuid") UUID uuid, @Param("isActive") boolean isActive);

    @Update("UPDATE product SET product_name = #{productName}, description = #{description}, " +
            "category_id = #{categoryId}, is_active = #{isActive} WHERE id = #{id}")
    void update(Product product);

    // --- Fetch Subcategory (Category) and Main Category ---
    @Select("SELECT " +
            "p.id, p.product_uuid, p.product_name, p.description, p.category_id, p.is_active, " +
            "u.email as seller_email, " +
            "c.category_name as categoryName, " +
            "mc.main_category_name as mainCategoryName, " +
            "COALESCE(AVG(r.rating), 0) as averageRating, " +
            "MIN(pv.price) as minPrice, " +
            "MAX(pv.price) as maxPrice, " +
            "(SELECT pi.image_url FROM product_image pi WHERE pi.product_id = p.id AND pi.is_main = true LIMIT 1) as mainImage " +
            "FROM product p " +
            "LEFT JOIN product_variant pv ON p.id = pv.product_id " +
            "LEFT JOIN users u ON p.seller_id = u.id " +
            "LEFT JOIN category c ON p.category_id = c.id " +
            "LEFT JOIN main_category mc ON c.main_category_id = mc.id " +
            "LEFT JOIN review r ON p.id = r.product_id " +
            "WHERE p.is_active = true " +
            "GROUP BY p.id, p.product_uuid, p.product_name, p.description, p.category_id, p.is_active, p.created_at, u.email, c.category_name, mc.main_category_name " +
            "ORDER BY p.created_at DESC " +
            "LIMIT #{limit} OFFSET #{offset}")
    @Results({
            @Result(property = "id", column = "id"),
            @Result(property = "productUuid", column = "product_uuid"),
            @Result(property = "productName", column = "product_name"),
            @Result(property = "categoryId", column = "category_id"),
            @Result(property = "categoryName", column = "categoryName"),
            @Result(property = "mainCategoryName", column = "mainCategoryName"),
            @Result(property = "averageRating", column = "averageRating"),
            @Result(property = "minPrice", column = "minPrice"),
            @Result(property = "maxPrice", column = "maxPrice"),
            @Result(property = "mainImage", column = "mainImage"),
            @Result(property = "sellerId", column = "seller_email")
    })
    List<ProductResponse> findAllActiveProductsSummary(@Param("limit") int limit, @Param("offset") int offset);

    // --- Search Query ---
    @Select("<script>" +
            "SELECT " +
            "p.id, p.product_uuid, p.product_name, p.description, p.category_id, p.is_active, " +
            "u.email as seller_email, " +
            "c.category_name as categoryName, " +
            "mc.main_category_name as mainCategoryName, " +
            "COALESCE(AVG(r.rating), 0) as averageRating, " +
            "MIN(pv.price) as minPrice, " +
            "MAX(pv.price) as maxPrice, " +
            "(SELECT pi.image_url FROM product_image pi WHERE pi.product_id = p.id AND pi.is_main = true LIMIT 1) as mainImage " +
            "FROM product p " +
            "LEFT JOIN product_variant pv ON p.id = pv.product_id " +
            "LEFT JOIN users u ON p.seller_id = u.id " +
            "LEFT JOIN category c ON p.category_id = c.id " +
            "LEFT JOIN main_category mc ON c.main_category_id = mc.id " +
            "LEFT JOIN review r ON p.id = r.product_id " +
            "WHERE p.id IN " +
            "<foreach item='id' collection='ids' open='(' separator=',' close=')'>" +
            "#{id}" +
            "</foreach> " +
            "GROUP BY p.id, p.product_uuid, p.product_name, p.description, p.category_id, p.is_active, p.created_at, u.email, c.category_name, mc.main_category_name " +
            "</script>")
    @Results({
            @Result(property = "id", column = "id"),
            @Result(property = "productUuid", column = "product_uuid"),
            @Result(property = "productName", column = "product_name"),
            @Result(property = "categoryId", column = "category_id"),
            @Result(property = "categoryName", column = "categoryName"),
            @Result(property = "mainCategoryName", column = "mainCategoryName"),
            @Result(property = "averageRating", column = "averageRating"),
            @Result(property = "minPrice", column = "minPrice"),
            @Result(property = "maxPrice", column = "maxPrice"),
            @Result(property = "mainImage", column = "mainImage"),
            @Result(property = "sellerId", column = "seller_email")
    })
    List<ProductResponse> findAllSummaryByIds(@Param("ids") List<Long> ids);

    @Select("SELECT p.id, p.product_uuid, p.product_name, p.description, u.email as seller_email, " +
            "c.category_name as categoryName, mc.main_category_name as mainCategoryName, " +
            "COALESCE(AVG(r.rating), 0) as averageRating, MIN(pv.price) as minPrice, MAX(pv.price) as maxPrice " +
            "FROM product p " +
            "LEFT JOIN product_variant pv ON p.id = pv.product_id " +
            "LEFT JOIN users u ON p.seller_id = u.id " +
            "LEFT JOIN category c ON p.category_id = c.id " +
            "LEFT JOIN main_category mc ON c.main_category_id = mc.id " +
            "LEFT JOIN review r ON p.id = r.product_id " +
            "WHERE p.id = #{productId} " +
            "GROUP BY p.id, u.email, c.category_name, mc.main_category_name")
    ProductResponse findSummaryById(@Param("productId") Long productId);




}