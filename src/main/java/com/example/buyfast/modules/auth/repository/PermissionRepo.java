package com.example.buyfast.modules.auth.repository;

import com.example.buyfast.modules.auth.model.Permission;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;

import java.util.Optional;

@Mapper
public interface PermissionRepo {

    @Select("SELECT * FROM permission WHERE permission_name = #{name}")
    Optional<Permission> findByName(String name);

    @Insert("INSERT INTO permission (permission_uuid, permission_name, description, resource_group, created_at) " +
            "VALUES (#{permissionUuid}, #{permissionName}, #{description}, #{resourceGroup}, CURRENT_TIMESTAMP)")
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    void save(Permission permission);
}