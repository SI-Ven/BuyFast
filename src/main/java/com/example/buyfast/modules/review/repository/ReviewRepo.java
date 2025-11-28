package com.example.buyfast.modules.review.repository;

import com.example.buyfast.modules.review.model.Review;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Mapper
public interface ReviewRepo {

    @Insert("INSERT INTO review (review_uuid, product_id, user_id, rating, review_text, review_date) " +
            "VALUES (#{reviewUuid}, #{productId}, #{userId}, #{rating}, #{reviewText}, #{reviewDate})") // <--- Change this part
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void save(Review review);

    @Select("SELECT r.*, up.first_name || ' ' || up.last_name as userName, up.user_profile as userAvatar " +
            "FROM review r " +
            "JOIN users u ON r.user_id = u.id " +
            "LEFT JOIN user_profile up ON u.id = up.user_id " +
            "WHERE r.product_id = #{productId} " +
            "ORDER BY r.review_date DESC")
    List<Review> findAllByProductId(Long productId);

    @Select("SELECT COUNT(*) > 0 FROM orders o " +
            "JOIN order_items oi ON o.id = oi.order_id " +
            "WHERE o.user_id = #{userId} AND oi.product_id = #{productId} AND o.status = 'delivered'")
    boolean hasPurchasedProduct(@Param("userId") Long userId, @Param("productId") Long productId);

    // Updates the company rating average when a new review is added
    @Update("UPDATE company c SET rating_average = " +
            "(SELECT AVG(r.rating) FROM review r JOIN product p ON r.product_id = p.id WHERE p.company_id = c.id) " +
            "WHERE c.id = (SELECT company_id FROM product WHERE id = #{productId})")
    void updateCompanyRating(Long productId);

    @Select("SELECT * FROM review WHERE review_uuid = #{reviewUuid}")
    Optional<Review> findByUuid(UUID reviewUuid);

    // 2. Delete the review
    @Delete("DELETE FROM review WHERE id = #{id}")
    void deleteById(Long id);
}