package com.example.buyfast.modules.auth.repository;

import com.example.buyfast.modules.auth.model.Role;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.Optional;

@Mapper
public interface RoleRepo {

    @Select("SELECT * FROM role WHERE role_name = #{roleName}")
    Optional<Role> findByRoleName(@Param("roleName") String roleName);

    // --- NEW: Allow saving a Role from Java ---
    @Insert("INSERT INTO role (role_uuid, role_name, description, scope, created_at) " +
            "VALUES (#{roleUuid}, #{roleName}, #{description}, #{scope}, CURRENT_TIMESTAMP)")
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    void save(Role role);

    @Insert("INSERT INTO user_role (user_id, role_id) VALUES (#{userId}, #{roleId})")
    void insertUserRole(@Param("userId") Long userId, @Param("roleId") Long roleId);
}