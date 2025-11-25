package com.example.buyfast.modules.user.repository;

import com.example.buyfast.config.mybatis.UuidTypeHandler; // Import your handler
import com.example.buyfast.modules.user.model.User;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Mapper
public interface UserRepo {

    @Insert("INSERT INTO users (user_uuid, email, user_password, status, created_at, company_id, token_version) " +
            "VALUES (#{userUuid}, #{email}, #{userPassword}, #{status}, CURRENT_TIMESTAMP, #{companyId}, 0)")
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    void save(User user);

    // --- FIXED: FETCH ROLES AND PERMISSIONS ---
    @Select("SELECT * FROM users WHERE email = #{email}")
    @Results({
            @Result(property = "id", column = "id"),
            @Result(property = "userUuid", column = "user_uuid", typeHandler = UuidTypeHandler.class),
            @Result(property = "roles", column = "id",
                    many = @Many(select = "com.example.buyfast.modules.auth.repository.RoleRepo.findRolesByUserId"))
    })
    Optional<User> findByEmail(String email);

    @Update("UPDATE users SET status = #{status} WHERE email = #{email}")
    void updateUserStatus(String email, String status);

    @Update("UPDATE users SET last_login = CURRENT_TIMESTAMP WHERE id = #{id}")
    void updateLastLogin(Long id);

    @Update("UPDATE users SET token_version = token_version + 1 WHERE id = #{id}")
    void incrementTokenVersion(Long id);

    @Select("SELECT * FROM users WHERE user_uuid = #{uuid}")
    @Results({
            @Result(property = "id", column = "id"),
            @Result(property = "userUuid", column = "user_uuid", typeHandler = UuidTypeHandler.class),
            @Result(property = "roles", column = "id",
                    many = @Many(select = "com.example.buyfast.modules.auth.repository.RoleRepo.findRolesByUserId"))
    })
    Optional<User> findByUuid(UUID uuid);

    @Update("UPDATE users SET user_password = #{newPassword} WHERE email = #{email}")
    void updatePassword(String email, String newPassword);

    @Update("UPDATE users SET phone_verified = true WHERE id = #{id}")
    void setPhoneVerified(Long id);

    @Update("UPDATE users SET verified = #{isVerified} WHERE id = #{id}")
    void setVerifiedStatus(@Param("id") Long id, @Param("isVerified") boolean isVerified);

    @Update("UPDATE users SET company_id = #{companyId} WHERE id = #{userId}")
    void updateUserCompanyId(@Param("userId") Long userId, @Param("companyId") Long companyId);

    @Delete("DELETE FROM users WHERE id = #{userId}")
    void deleteById(Long userId);

    @Select("SELECT u.* FROM users u " +
            "JOIN user_role ur ON u.id = ur.user_id " +
            "JOIN role r ON ur.role_id = r.id " +
            "WHERE u.company_id = #{companyId} AND r.role_name = 'seller_company'")
    List<User> findSellersByCompanyId(Long companyId);

    @Select("SELECT COUNT(u.id) FROM users u " +
            "JOIN user_role ur ON u.id = ur.user_id " +
            "JOIN role r ON ur.role_id = r.id " +
            "WHERE u.company_id = #{companyId} AND r.role_name = 'seller_company'")
    int countSellersByCompanyId(Long companyId);

    @Select("SELECT * FROM users ORDER BY created_at DESC")
    List<User> findAllUsers();

    @Update("UPDATE users SET status = #{status} WHERE user_uuid = #{uuid}")
    void updateUserStatusByUuid(@Param("uuid") UUID uuid, @Param("status") String status);

    @Update("UPDATE users SET phone_number = #{phoneNumber}, phone_verified = false WHERE id = #{id}")
    void updateUserPhoneNumber(@Param("id") Long id, @Param("phoneNumber") String phoneNumber);
}