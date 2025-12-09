package com.example.buyfast.modules.user.repository;

import com.example.buyfast.modules.user.model.UserProfile;
import org.apache.ibatis.annotations.*;

import java.util.Optional;

@Mapper
public interface UserProfileRepo {

    @Select("SELECT * FROM user_profile WHERE user_id = #{userId}")
    @Results({
            @Result(property = "postalCode", column = "Postal_Code"),
            @Result(property = "userProfile", column = "user_profile"),
            @Result(property = "firstName", column = "first_name"),
            @Result(property = "lastName", column = "last_name"),
            @Result(property = "userName", column = "user_name"),
            @Result(property = "phoneNumber", column = "phone_number"),
            @Result(property = "userId", column = "user_id"),
            @Result(property = "createdAt", column = "created_at"),
            @Result(property = "updatedAt", column = "updated_at")
    })
    Optional<UserProfile> findByUserId(Long userId);

    @Insert("INSERT INTO user_profile (user_id, first_name, last_name, user_name, email, phone_number, user_profile, address, city, country, Postal_Code) " +
            "VALUES (#{userId}, #{firstName}, #{lastName}, #{userName}, #{email}, #{phoneNumber}, #{userProfile}, #{address}, #{city}, #{country}, #{postalCode})")
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    void create(UserProfile userProfile);

    @Update("UPDATE user_profile SET " +
            "first_name = #{firstName}, " +
            "last_name = #{lastName}, " +
            "user_name = #{userName}, " +
            "email = #{email}, " +
            "phone_number = #{phoneNumber}, " +
            "user_profile = #{userProfile}, " +
            "cover_profile = #{coverProfile}, " +
            "address = #{address}, " +
            "city = #{city}, " +
            "country = #{country}, " +
            "Postal_Code = #{postalCode}, " +
            "updated_at = CURRENT_TIMESTAMP " +
            "WHERE user_id = #{userId}")
    void update(UserProfile userProfile);

    @Delete("DELETE FROM user_profile WHERE user_id = #{userId}")
    void deleteByUserId(Long userId);
}