package com.example.buyfast.modules.user.repository;

import com.example.buyfast.modules.user.model.Permission;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Mapper
public interface PermissionRepository {

    @Select("SELECT id as permissionId, permission_key as permissionKey, description FROM auth.permissions")
    List<Permission> findAll();

    @Select("SELECT id as permissionId, permission_key as permissionKey, description FROM auth.permissions WHERE id = #{permissionId}")
    Optional<Permission> findById(@Param("permissionId") Integer permissionId);

    @Select("SELECT id as permissionId, permission_key as permissionKey, description FROM auth.permissions WHERE permission_key = #{permissionKey}")
    Optional<Permission> findByKey(@Param("permissionKey") String permissionKey);

    // Used to load permissions for a role
    @Select("SELECT p.id as permissionId, p.permission_key as permissionKey, p.description " +
            "FROM auth.permissions p " +
            "INNER JOIN auth.role_permissions rp ON p.id = rp.permission_id " +
            "WHERE rp.role_id = #{roleId}")
    Set<Permission> findPermissionsByRoleId(@Param("roleId") Integer roleId);

    @Insert("INSERT INTO auth.permissions (permission_key, description) VALUES (#{permissionKey}, #{description})")
    @Options(useGeneratedKeys = true, keyProperty = "permissionId", keyColumn = "id")
    void insert(Permission permission);

    @Update("UPDATE auth.permissions SET permission_key = #{permissionKey}, description = #{description} WHERE id = #{permissionId}")
    int update(Permission permission);

    @Delete("DELETE FROM auth.permissions WHERE id = #{permissionId}")
    int deleteById(@Param("permissionId") Integer permissionId);
}