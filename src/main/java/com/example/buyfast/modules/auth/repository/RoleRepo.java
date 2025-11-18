package com.example.buyfast.modules.auth.repository;

import com.example.buyfast.modules.auth.model.Permission;
import com.example.buyfast.modules.auth.model.Role;
import com.example.buyfast.config.mybatis.UuidTypeHandler;
import org.apache.ibatis.annotations.*;

import java.util.Optional;
import java.util.Set;

@Mapper
public interface RoleRepo {

    @Select("SELECT * FROM role WHERE role_name = #{roleName}")
    Optional<Role> findByRoleName(@Param("roleName") String roleName);

    @Insert("INSERT INTO role (role_uuid, role_name, description, scope, created_at) " +
            "VALUES (#{roleUuid}, #{roleName}, #{description}, #{scope}, CURRENT_TIMESTAMP)")
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    void save(Role role);

    @Insert("INSERT INTO user_role (user_id, role_id) VALUES (#{userId}, #{roleId})")
    void insertUserRole(@Param("userId") Long userId, @Param("roleId") Long roleId);

    // --- NEW: Link Role to Permission ---
    @Insert("INSERT INTO role_permission (role_id, permission_id) VALUES (#{roleId}, #{permissionId}) " +
            "ON CONFLICT (role_id, permission_id) DO NOTHING")
    void addPermissionToRole(@Param("roleId") Long roleId, @Param("permissionId") Long permissionId);

    // --- EXISTING EAGER LOADING METHODS ---
    @Select("SELECT p.* FROM permission p " +
            "JOIN role_permission rp ON p.id = rp.permission_id " +
            "WHERE rp.role_id = #{roleId}")
    Set<Permission> findPermissionsByRoleId(Long roleId);

    @Select("SELECT r.* FROM role r " +
            "JOIN user_role ur ON r.id = ur.role_id " +
            "WHERE ur.user_id = #{userId}")
    @Results({
            @Result(property = "id", column = "id"),
            @Result(property = "roleUuid", column = "role_uuid", typeHandler = UuidTypeHandler.class),
            @Result(property = "permissions", column = "id",
                    many = @Many(select = "findPermissionsByRoleId"))
    })
    Set<Role> findRolesByUserId(Long userId);
}