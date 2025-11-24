package com.example.buyfast.modules.user.repository;

import com.example.buyfast.modules.user.model.Role;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Mapper
public interface RoleRepository {

    // --- 1. FIXED: Added SQL annotation so MyBatis can find the statement ---
    @Select("SELECT * FROM auth.roles WHERE name = #{name}")
    @Results(id = "roleResultMap", value = {
            @Result(property = "roleId", column = "id", id = true), // Ensure property matches your Role class field (e.g. 'id' or 'roleId')
            @Result(property = "name", column = "name"),
            @Result(property = "description", column = "description"),
            @Result(property = "isActive", column = "is_active"),
            @Result(property = "createdAt", column = "created_at"),
            @Result(property = "updatedAt", column = "updated_at"),
            @Result(property = "createdBy", column = "created_by")
    })
    Optional<Role> findByName(@Param("name") String name);

    // --- 2. UPDATED: Reuse the ResultMap for other reads ---
    @Select("SELECT * FROM auth.roles WHERE id = #{roleId}")
    @ResultMap("roleResultMap")
    Optional<Role> findById(@Param("roleId") Integer roleId);

    @Select("SELECT * FROM auth.roles")
    @ResultMap("roleResultMap")
    List<Role> findAll();

    // Used internally by UserMapper.xml, typically doesn't need annotation if mapped in XML,
    // but if you want to use it in Java code, you can add a simple select:
    @Select("SELECT r.* FROM auth.roles r " +
            "JOIN auth.user_roles ur ON r.id = ur.role_id " +
            "WHERE ur.user_id = #{userId}")
    @ResultMap("roleResultMap")
    Set<Role> findRolesByUserId(@Param("userId") Long userId);

    // --- WRITES (These look good, just ensure property names match Role.java) ---
    @Insert("INSERT INTO auth.roles (name, description, is_active, created_by) " +
            "VALUES (#{name}, #{description}, #{isActive}, #{createdBy})")
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id") // Changed 'roleId' to 'id' to match standard convention if you updated Role.java
    int insert(Role role);

    @Update("UPDATE auth.roles SET name = #{name}, description = #{description}, is_active = #{isActive} WHERE id = #{id}")
    int update(Role role);

    @Delete("DELETE FROM auth.roles WHERE id = #{roleId}")
    int deleteById(@Param("roleId") Integer roleId);

    // --- USER_ROLES JOIN TABLE MANAGEMENT ---
    @Insert("INSERT INTO auth.user_roles (user_id, role_id) VALUES (#{userId}, #{roleId}) ON CONFLICT DO NOTHING")
    int addRoleToUser(@Param("userId") Long userId, @Param("roleId") Integer roleId);

    @Select("SELECT COUNT(*) FROM auth.user_roles WHERE user_id = #{userId} AND role_id = #{roleId}")
    int countUserRoleAssociation(@Param("userId") Long userId, @Param("roleId") Integer roleId);

    @Delete("DELETE FROM auth.user_roles WHERE user_id = #{userId}")
    int removeAllRolesFromUser(@Param("userId") Long userId);

    @Delete("DELETE FROM auth.user_roles WHERE user_id = #{userId} AND role_id = #{roleId}")
    int removeRoleFromUser(@Param("userId") Long userId, @Param("roleId") Integer roleId);
}