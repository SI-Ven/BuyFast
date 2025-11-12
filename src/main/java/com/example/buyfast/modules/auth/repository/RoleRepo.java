package com.example.buyfast.modules.auth.repository;// package com.example.buyfast.modules.auth.repository;
import com.example.buyfast.modules.auth.model.Role;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Optional;

@Mapper
public interface RoleRepo {

    Optional<Role> findByRoleName(@Param("roleName") String roleName);

    // This is for assigning a role to a user
    void insertUserRole(@Param("userId") Long userId, @Param("roleId") Long roleId);
}