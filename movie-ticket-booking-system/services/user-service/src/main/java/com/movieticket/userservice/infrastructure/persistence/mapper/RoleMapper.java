package com.movieticket.userservice.infrastructure.persistence.mapper;

import com.movieticket.userservice.domain.entity.Role;
import com.movieticket.userservice.infrastructure.persistence.entity.RoleJpaEntity;

import java.lang.reflect.Field;

public class RoleMapper {

    private RoleMapper() {
    }

    public static RoleJpaEntity toJpa(Role role) {

        RoleJpaEntity entity = new RoleJpaEntity();

        if (role.getId() != null) {
            entity.setId(role.getId().intValue());
        }

        entity.setRoleName(role.getRoleName());
        entity.setDescription(role.getDescription());

        return entity;
    }

    public static Role toDomain(RoleJpaEntity entity) {

        Role role = Role.create(
                entity.getRoleName(),
                entity.getDescription()
        );

        setField(role, "id", entity.getId().longValue());

        return role;
    }

    private static void setField(
            Object target,
            String fieldName,
            Object value
    ) {

        try {

            Field field = target.getClass()
                    .getDeclaredField(fieldName);

            field.setAccessible(true);

            field.set(target, value);

        } catch (Exception e) {

            throw new RuntimeException(e);

        }
    }
}