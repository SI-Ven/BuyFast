package com.example.buyfast.modules.user.service.impl;

import com.example.buyfast.modules.user.model.Permission;
import com.example.buyfast.modules.user.repository.PermissionRepository;
import com.example.buyfast.modules.user.service.PermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class PermissionServiceImpl implements PermissionService {
    private final PermissionRepository permissionRepository;

    @Override
    public List<Permission> getAllPermissions() {
        return permissionRepository.findAll();
    }

    @Override
    public Optional<Permission> getPermissionById(Integer id) {
        return permissionRepository.findById(id);
    }

    @Override
    public Optional<Permission> getPermissionByKey(String key) {
        return permissionRepository.findByKey(key);
    }

    @Override
    public void createPermission(Permission permission) {
        permissionRepository.insert(permission);
    }

    @Override
    public void updatePermission(Permission permission) {
        permissionRepository.update(permission);
    }

    @Override
    public void deletePermission(Integer id) {
        permissionRepository.deleteById(id);
    }
}
