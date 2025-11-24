package com.example.buyfast.modules.user.repository;

import com.example.buyfast.modules.user.model.User;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Mapper
public interface UserRepository {

    // --- READ METHODS (Mapped in UserMapper.xml for High Performance Joins) ---
    // We remove @Select here because the SQL is complex and lives in the XML file
    Optional<User> findById(@Param("id") Long id);

    Optional<User> findByPublicId(@Param("publicId") UUID publicId);

    Optional<User> findByUsername(@Param("username") String username);

    Optional<User> findByEmail(@Param("email") String email);

    List<User> findAll();

    List<User> findByIsActive(@Param("isActive") Boolean isActive);

    // --- WRITE METHODS (Simple enough for Annotations) ---

    @Insert("INSERT INTO auth.users (public_id, username, email, password, is_active) " +
            "VALUES (#{publicId}::uuid, #{username}, #{email}, #{password}, #{isActive})")
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    int insert(User user);

    @Update("UPDATE auth.users " +
            "SET username = #{username}, " +
            "    email = #{email}, " +
            "    is_active = #{isActive}, " +
            "    updated_at = #{updatedAt} " +
            "WHERE id = #{id}")
    int update(User user);

    @Update("UPDATE auth.users " +
            "SET password = #{password}, " +
            "    updated_at = CURRENT_TIMESTAMP " +
            "WHERE id = #{id}")
    int updatePassword(@Param("id") Long id, @Param("password") String password);

    @Delete("DELETE FROM auth.users WHERE id = #{id}")
    int deleteById(@Param("id") Long id);

    // --- UTILITY / CHECKS ---

    @Select("SELECT EXISTS(SELECT 1 FROM auth.users WHERE username = #{username})")
    boolean existsByUsername(String username);

    @Select("SELECT EXISTS(SELECT 1 FROM auth.users WHERE email = #{email})")
    boolean existsByEmail(String email);

    @Select("SELECT EXISTS(SELECT 1 FROM auth.users WHERE public_id = #{publicId}::uuid)")
    boolean existsByPublicId(UUID publicId);

    @Select("SELECT COUNT(*) FROM auth.users WHERE is_active = true")
    long countActiveUsers();

    @Select("SELECT COUNT(*) FROM auth.users")
    long countAllUsers();

    // --- ROLE MANAGEMENT (Manual Join Table Operations) ---

    @Insert("INSERT INTO auth.user_roles (user_id, role_id) VALUES (#{userId}, #{roleId}) ON CONFLICT DO NOTHING")
    int addRoleToUser(@Param("userId") Long userId, @Param("roleId") Integer roleId);

    @Delete("DELETE FROM auth.user_roles WHERE user_id = #{userId} AND role_id = #{roleId}")
    int removeRoleFromUser(@Param("userId") Long userId, @Param("roleId") Integer roleId);

    @Delete("DELETE FROM auth.user_roles WHERE user_id = #{userId}")
    int removeAllRolesFromUser(@Param("userId") Long userId);

    @Update("UPDATE auth.users SET is_active = true, updated_at = CURRENT_TIMESTAMP WHERE email = #{email}")
    int enableUserByEmail(@Param("email") String email);
}