package com.movieticket.userservice.infrastructure.persistence.mapper;

import com.movieticket.userservice.domain.entity.RefreshToken;
import com.movieticket.userservice.infrastructure.persistence.entity.RefreshTokenJpaEntity;

import java.lang.reflect.Field;

public class RefreshTokenMapper {

    private RefreshTokenMapper() {
    }

    public static RefreshTokenJpaEntity toJpa(
            RefreshToken token
    ) {

        RefreshTokenJpaEntity entity =
                new RefreshTokenJpaEntity();

        entity.setId(token.getId());
        entity.setToken(token.getToken());
        entity.setUserId(token.getUserId());
        entity.setExpiryDate(token.getExpiryDate());
        entity.setRevoked(token.getRevoked());
        entity.setCreatedAt(token.getCreatedAt());

        return entity;
    }

    public static RefreshToken toDomain(
            RefreshTokenJpaEntity entity
    ) {

        RefreshToken token =
                RefreshToken.create(
                        entity.getToken(),
                        entity.getUserId(),
                        entity.getExpiryDate()
                );

        setField(token, "id", entity.getId());
        setField(token, "revoked", entity.getRevoked());
        setField(token, "createdAt", entity.getCreatedAt());

        return token;
    }

    private static void setField(
            Object target,
            String name,
            Object value
    ) {

        try {

            Field field =
                    target.getClass()
                            .getDeclaredField(name);

            field.setAccessible(true);

            field.set(target, value);

        } catch (Exception e) {

            throw new RuntimeException(e);

        }

    }
}