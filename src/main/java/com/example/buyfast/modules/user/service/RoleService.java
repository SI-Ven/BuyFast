package com.example.buyfast.modules.user.service;

import com.example.buyfast.modules.user.model.Permission;
import com.example.buyfast.modules.user.model.Role;

import java.util.List;
import java.util.Optional;

public interface RoleService {
    List<Role> getAllRoles();
    Optional<Role> getRoleById(Integer id);
    Optional<Role> getRoleByName(String name);
}
