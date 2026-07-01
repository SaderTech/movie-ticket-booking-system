package com.movieticket.userservice.domain.repository;

import com.movieticket.userservice.domain.entity.Role;

import java.util.List;
import java.util.Optional;

public interface RoleRepository {

    Role save(Role role);

    Optional<Role> findById(Long id);

    Optional<Role> findByRoleName(String roleName);

    List<Role> findAll();
}