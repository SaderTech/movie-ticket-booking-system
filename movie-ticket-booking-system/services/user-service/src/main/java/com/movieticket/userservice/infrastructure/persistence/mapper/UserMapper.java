package com.movieticket.userservice.infrastructure.persistence.mapper;

import com.movieticket.userservice.domain.entity.User;
import com.movieticket.userservice.domain.valueobject.Email;
import com.movieticket.userservice.domain.valueobject.FullName;
import com.movieticket.userservice.domain.valueobject.Password;
import com.movieticket.userservice.domain.valueobject.PhoneNumber;
import com.movieticket.userservice.infrastructure.persistence.entity.UserJpaEntity;

public class UserMapper {

    private UserMapper() {}

    // =========================
    // DOMAIN → JPA
    // =========================
    public static UserJpaEntity toJpa(User user) {

        UserJpaEntity entity = new UserJpaEntity();

        entity.setId(user.getId());
        entity.setUsername(user.getUsername());
        entity.setEmail(user.getEmail().value());
        entity.setPassword(user.getPassword().value());
        entity.setFullName(user.getFullName().value());

        entity.setPhone(
                user.getPhone() == null ? null : user.getPhone().value()
        );

        entity.setAvatar(user.getAvatar());
        entity.setDateOfBirth(user.getDateOfBirth());
        entity.setGender(user.getGender());

        // ⚠️ QUAN TRỌNG: JPA field là isActive
        entity.setIsActive(user.getActive());

        entity.setCreatedAt(user.getCreatedAt());
        entity.setUpdatedAt(user.getUpdatedAt());

        return entity;
    }

    // =========================
    // JPA → DOMAIN
    // =========================
    public static User toDomain(UserJpaEntity entity) {

        return User.restore(
                entity.getId(),
                entity.getUsername(),
                new Email(entity.getEmail()),
                new Password(entity.getPassword()),
                new FullName(entity.getFullName()),
                entity.getPhone() == null ? null : new PhoneNumber(entity.getPhone()),
                entity.getAvatar(),
                entity.getDateOfBirth(),
                entity.getGender(),
                entity.getIsActive(),   // ⚠️ phải là getIsActive()
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}