package com.xtremand.user.role.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.xtremand.domain.entity.Role;
import com.xtremand.user.role.repository.RoleRepository;


/**
 * Service implementation for managing roles.
 */
@Service
@Transactional
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;

    public RoleServiceImpl(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @Override
    public Role createRole(String name) {
        Optional<Role> existing = roleRepository.findByName(name);
        if (existing.isPresent()) {
            throw new RoleAlreadyExistsException("Role with name '" + name + "' already exists");
        }
        Role role = Role.builder().name(name).build();
        return roleRepository.save(role);
    }

    @Override
    public List<Role> getAllRoles() {
        return roleRepository.findAll();
    }

    @Override
    public List<String> getAllRoleKeys() {
        return roleRepository.findAll().stream().map(Role::getRoleKey).collect(Collectors.toList());
    }

    @Override
    public Optional<Role> getRoleByName(String name) {
        return roleRepository.findByName(name);
    }
}