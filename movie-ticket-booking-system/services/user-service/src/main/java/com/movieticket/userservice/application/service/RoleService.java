package com.movieticket.userservice.application.service;

import com.movieticket.userservice.domain.entity.Role;

import java.util.List;

public interface RoleService {

    Role createRole(
            String roleName,
            String description
    );

    Role getById(Long id);

    Role getByName(String roleName);

    List<Role> getAll();

    void delete(Long id);
}