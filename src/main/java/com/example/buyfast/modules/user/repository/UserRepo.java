package com.example.buyfast.modules.user.repository;

import com.example.buyfast.modules.user.model.User;
import org.apache.ibatis.annotations.*;

import java.util.Optional;

@Mapper
public interface UserRepo {

    @Insert("INSERT INTO users (user_uuid, first_name, last_name, dob, address, email, user_password, role, status, created_at) " +
            "VALUES (#{userUuid}, #{firstName}, #{lastName}, #{dob}, #{address}, #{email}, #{userPassword}, #{role}, #{status}, CURRENT_TIMESTAMP)")
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    void save(User user);

    @Select("SELECT * FROM users WHERE email = #{email}")
    Optional<User> findByEmail(String email);

    @Update("UPDATE users SET status = #{status} WHERE email = #{email}")
    void updateUserStatus(String email, String status);

    @Update("UPDATE users SET last_login = CURRENT_TIMESTAMP WHERE id = #{id}")
    void updateLastLogin(Long id);

    @Update("UPDATE users SET user_password = #{newPassword} WHERE email = #{email}")
    void updatePassword(String email, String newPassword);
}