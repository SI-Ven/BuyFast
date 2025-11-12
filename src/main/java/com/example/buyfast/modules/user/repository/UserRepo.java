package com.example.buyfast.modules.user.repository;

import com.example.buyfast.modules.user.model.User;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Mapper
public interface UserRepo {

    /**
     * MODIFIED: This query now only inserts data that is still on the
     * User model. The 'role' column is also removed, as that logic
     * will move to a new UserRoleRepo.
     */
    @Insert("INSERT INTO users (user_uuid, email, user_password, status, created_at, company_id) " +
            "VALUES (#{userUuid}, #{email}, #{userPassword}, #{status}, CURRENT_TIMESTAMP, #{companyId})")
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    void save(User user);

    /**
     * This method needs a <ResultMap> in an XML file to correctly
     * populate the nested UserProfile and Set<Role> objects.
     * The simple @Select will only populate the User object's flat fields.
     */
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

    // --- DELETED 'updateProfile' method ---
    // This logic is now handled by UserProfileRepo.update()

    // --- DELETED 'updateUserRole' method ---
    // This logic must move to a new UserRoleRepo

    @Update("UPDATE users SET phone_verified = true WHERE id = #{id}")
    void setPhoneVerified(Long id);

    @Update("UPDATE users SET verified = #{isVerified} WHERE id = #{id}")
    void setVerifiedStatus(@Param("id") Long id, @Param("isVerified") boolean isVerified);

    // --- DELETED 'updateUserRoleAndCompany' method ---
    // This logic must be split. Role changes move to UserRoleRepo.

    @Update("UPDATE users SET company_id = #{companyId} WHERE id = #{userId}")
    void updateUserCompanyId(@Param("userId") Long userId, @Param("companyId") Long companyId);

    // --- DELETED 'countSellersByCompanyId' method ---
    // This logic must be refactored to query the new user_roles table

    // --- DELETED 'findSellersByCompanyId' method ---
    // This logic must be refactored to query the new user_roles table

    @Delete("DELETE FROM users WHERE id = #{userId}")
    void deleteById(Long userId);

    // --- DELETED 'updateSellerProfile' method ---
    // This logic is now handled by UserProfileRepo.update()

    // --- NEW METHODS FOR SUPER ADMIN ---

    @Select("SELECT * FROM users ORDER BY created_at DESC")
    List<User> findAllUsers();

    @Update("UPDATE users SET status = #{status} WHERE user_uuid = #{uuid}")
    void updateUserStatusByUuid(@Param("uuid") UUID uuid, @Param("status") String status);
}