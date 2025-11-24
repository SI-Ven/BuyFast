package com.example.buyfast.modules.user.service.impl;

import com.example.buyfast.modules.user.model.Permission;
import com.example.buyfast.modules.user.model.Role;
import com.example.buyfast.modules.user.repository.RoleRepository;
import com.example.buyfast.modules.user.service.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class RoleServiceImpl implements RoleService {
    private final RoleRepository roleRepository;

    @Override
    @Transactional(readOnly = true)
    public List<Role> getAllRoles() {
        return roleRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Role> getRoleById(Integer id) {
        return roleRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true) // Added readOnly
    public Optional<Role> getRoleByName(String name) {
        return roleRepository.findByName(name);
    }
}