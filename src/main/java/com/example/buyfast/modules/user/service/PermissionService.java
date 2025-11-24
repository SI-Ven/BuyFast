package com.example.buyfast.modules.user.service;

import com.example.buyfast.modules.user.model.Permission;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

public interface PermissionService {
    List<Permission> getAllPermissions();
    Optional<Permission> getPermissionById(Integer id);
    Optional<Permission> getPermissionByKey(String key);
    void createPermission(Permission permission);
    void updatePermission(Permission permission);
    void deletePermission(Integer id);
}
