package com.movieticket.userservice.infrastructure.persistence.mapper;

import com.movieticket.userservice.domain.entity.UserRole;
import com.movieticket.userservice.infrastructure.persistence.entity.UserRoleJpaEntity;

public class UserRoleMapper {

    private UserRoleMapper() {
    }

    public static UserRoleJpaEntity toJpa(
            UserRole userRole
    ) {

        if (userRole == null) {
            return null;
        }

        UserRoleJpaEntity entity = new UserRoleJpaEntity();

        entity.setUserId(userRole.getUserId());
        entity.setRoleId(userRole.getRoleId());

        return entity;
    }


    public static UserRole toDomain(
            UserRoleJpaEntity entity
    ) {

        if (entity == null) {
            return null;
        }

        return UserRole.create(
                entity.getUserId(),
                entity.getRoleId()
        );
    }
}