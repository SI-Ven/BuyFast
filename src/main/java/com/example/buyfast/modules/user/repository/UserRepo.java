package com.example.buyfast.modules.user.repository;

import com.example.buyfast.modules.user.model.User;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Mapper
public interface UserRepo {

    @Insert("INSERT INTO users (user_uuid, first_name, last_name,user_name, email, user_password, role, status, created_at, company_id) " +
            "VALUES (#{userUuid}, #{firstName}, #{lastName},#{userName}, #{email}, #{userPassword}, #{role}, #{status}, CURRENT_TIMESTAMP, #{companyId})")
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    void save(User user);

    @Select("SELECT * FROM users WHERE email = #{email}")
    Optional<User> findByEmail(String email);

    @Update("UPDATE users SET status = #{status} WHERE email = #{email}")
    void updateUserStatus(String email, String status);

    @Update("UPDATE users SET last_login = CURRENT_TIMESTAMP WHERE id = #{id}")
    void updateLastLogin(Long id);
    @Select("SELECT * FROM users WHERE user_uuid = #{uuid}")
    Optional<User> findByUuid(UUID uuid);

    @Update("UPDATE users SET user_password = #{newPassword} WHERE email = #{email}")
    void updatePassword(String email, String newPassword);

    @Update("UPDATE users SET " +
            "first_name = #{firstName}, " +
            "last_name = #{lastName}, " +
            "user_name = #{userName}, " +
            "user_profile = #{userProfile}, " +
            "dob = #{dob}, " +
            "address = #{address}, " +
            "phone_number = #{phoneNumber} " +
            "WHERE id = #{id}")
    void updateProfile(User user);

    @Update("UPDATE users SET role = #{role} WHERE id = #{id}")
    void updateUserRole(@Param("id") Long id, @Param("role") String role);

    @Update("UPDATE users SET phone_verified = true WHERE id = #{id}")
    void setPhoneVerified(Long id);

    @Update("UPDATE users SET verified = #{isVerified} WHERE id = #{id}")
    void setVerifiedStatus(@Param("id") Long id, @Param("isVerified") boolean isVerified);

    // --- NEW METHODS FOR COMPANY ADMIN ---

    @Update("UPDATE users SET role = #{role}, company_id = #{companyId} WHERE id = #{userId}")
    void updateUserRoleAndCompany(@Param("userId") Long userId, @Param("role") String role, @Param("companyId") Long companyId);

    // --- NEW METHOD FOR REGISTER-COMPANY FLOW ---
    @Update("UPDATE users SET company_id = #{companyId} WHERE id = #{userId}")
    void updateUserCompanyId(@Param("userId") Long userId, @Param("companyId") Long companyId);

    @Select("SELECT COUNT(*) FROM users WHERE company_id = #{companyId} AND role = 'seller_company'")
    int countSellersByCompanyId(Long companyId);

    @Select("SELECT * FROM users WHERE company_id = #{companyId} AND role = 'seller_company'")
    List<User> findSellersByCompanyId(Long companyId);

    @Delete("DELETE FROM users WHERE id = #{userId}")
    void deleteById(Long userId);

    @Update("UPDATE users SET first_name = #{firstName}, last_name = #{lastName}, status = #{status} WHERE id = #{id}")
    void updateSellerProfile(@Param("id") Long id, @Param("firstName") String firstName, @Param("lastName") String lastName, @Param("status") String status);
}