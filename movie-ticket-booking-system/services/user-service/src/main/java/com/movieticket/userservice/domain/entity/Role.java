package com.movieticket.userservice.domain.entity;

import lombok.Getter;

import java.util.Objects;

@Getter
public class Role {

    private Long id;

    private String roleName;

    private String description;

    private Role() {
    }

    public static Role create(
            String roleName,
            String description
    ) {

        Objects.requireNonNull(roleName);

        Role role = new Role();

        role.roleName = roleName.trim().toUpperCase();
        role.description = description;

        return role;
    }
}