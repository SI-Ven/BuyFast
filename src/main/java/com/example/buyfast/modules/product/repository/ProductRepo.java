package com.example.buyfast.modules.product.repository;

import com.example.buyfast.modules.product.model.Product;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Mapper
public interface ProductRepo {

    // --- MODIFIED: Removed price and stock_quantity ---
    @Insert("INSERT INTO product (product_uuid, product_name, company_id, seller_id, category_id, " +
            "description, is_active, created_at) " +
            "VALUES (#{productUuid}, #{productName}, #{companyId}, #{sellerId}, #{categoryId}, " +
            "#{description}, #{isActive}, CURRENT_TIMESTAMP)")
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    void insert(Product product);

    // --- We no longer update the base product, we update variants ---
    // (Removed update method)

    // --- Find by UUID and Seller ID (for security) ---
    @Select("SELECT * FROM product WHERE product_uuid = #{productUuid} AND seller_id = #{sellerId}")
    Optional<Product> findByUuidAndSellerId(@Param("productUuid") UUID productUuid, @Param("sellerId") Long sellerId);

    // --- Find all products for a specific seller ---
    @Select("SELECT * FROM product WHERE seller_id = #{sellerId} ORDER BY created_at DESC")
    List<Product> findAllBySellerId(Long sellerId);

    // --- Delete a product by UUID and Seller ID (for security) ---
    // This will cascade and delete all options, values, and variants
    @Delete("DELETE FROM product WHERE product_uuid = #{productUuid} AND seller_id = #{sellerId}")
    void deleteByUuidAndSellerId(@Param("productUuid") UUID productUuid, @Param("sellerId") Long sellerId);

    // --- NEW METHOD ---
    @Select("SELECT * FROM product WHERE id = #{id}")
    Optional<Product> findById(Long id);
    // --- END NEW METHOD ---

    // --- Admin methods (unchanged) ---
    @Select("SELECT * FROM product WHERE product_uuid = #{uuid}")
    Optional<Product> findByUuid(UUID uuid);

    @Select("SELECT * FROM product ORDER BY created_at DESC")
    List<Product> findAllProducts();

    @Update("UPDATE product SET is_active = #{isActive} WHERE product_uuid = #{uuid}")
    void updateProductActiveStatus(@Param("uuid") UUID uuid, @Param("isActive") boolean isActive);
}