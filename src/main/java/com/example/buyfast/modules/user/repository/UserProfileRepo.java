package com.example.buyfast.modules.user.repository;

import com.example.buyfast.modules.user.model.UserProfile;
import org.apache.ibatis.annotations.*;

import java.util.Optional;

/**
 * NEW REPOSITORY INTERFACE
 * Required to correctly create, update, and delete profile data.
 */
@Mapper
public interface UserProfileRepo {

    @Select("SELECT * FROM user_profile WHERE user_id = #{userId}")
    Optional<UserProfile> findByUserId(Long userId);

    @Insert("INSERT INTO user_profile (user_id, first_name, last_name, user_name, user_profile, dob, address) " +
            "VALUES (#{userId}, #{firstName}, #{lastName}, #{userName}, #{userProfile}, #{dob}, #{address})")
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    void create(UserProfile userProfile);

    @Update("UPDATE user_profile SET first_name = #{firstName}, last_name = #{lastName}, " +
            "user_name = #{userName}, user_profile = #{userProfile}, dob = #{dob}, address = #{address}, " +
            "updated_at = CURRENT_TIMESTAMP WHERE user_id = #{userId}")
    void update(UserProfile userProfile);

    @Delete("DELETE FROM user_profile WHERE user_id = #{userId}")
    void deleteByUserId(Long userId);
}