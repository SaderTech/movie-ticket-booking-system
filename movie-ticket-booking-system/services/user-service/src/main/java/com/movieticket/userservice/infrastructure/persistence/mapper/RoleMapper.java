package com.movieticket.userservice.infrastructure.persistence.mapper;

import com.movieticket.userservice.domain.entity.Role;
import com.movieticket.userservice.infrastructure.persistence.entity.RoleJpaEntity;

import java.lang.reflect.Field;

public class RoleMapper {

    private RoleMapper() {
    }

    public static RoleJpaEntity toJpa(Role role) {

        if (role == null) {
            return null;
        }

        RoleJpaEntity entity = new RoleJpaEntity();

        entity.setId(role.getId());
        entity.setRoleName(role.getRoleName());
        entity.setDescription(role.getDescription());

        return entity;
    }

    public static Role toDomain(RoleJpaEntity entity) {

        if (entity == null) {
            return null;
        }

        Role role = Role.create(
                entity.getRoleName(),
                entity.getDescription()
        );

        setField(role, "id", entity.getId());

        return role;
    }

    private static void setField(
            Object target,
            String fieldName,
            Object value
    ) {

        try {

            Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);

        } catch (ReflectiveOperationException e) {

            throw new RuntimeException(
                    "Failed to set field '" + fieldName + "' on " + target.getClass().getSimpleName(),
                    e
            );

        }
    }
}