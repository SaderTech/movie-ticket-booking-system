package com.movieticket.userservice.infrastructure.persistence.mapper;

import com.movieticket.userservice.domain.entity.UserRole;
import com.movieticket.userservice.infrastructure.persistence.entity.UserRoleJpaEntity;

public class UserRoleMapper {

    private UserRoleMapper() {
    }

    public static UserRoleJpaEntity toJpa(
            UserRole role
    ) {

        UserRoleJpaEntity entity =
                new UserRoleJpaEntity();

        entity.setUserId(role.getUserId());
        entity.setRoleId(role.getRoleId().intValue());

        return entity;
    }

    public static UserRole toDomain(
            UserRoleJpaEntity entity
    ) {

        return UserRole.create(
                entity.getUserId(),
                entity.getRoleId().longValue()
        );
    }

}