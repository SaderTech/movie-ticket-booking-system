package com.movieticket.userservice.infrastructure.persistence.repository;

import com.movieticket.userservice.infrastructure.persistence.entity.UserRoleJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JpaUserRoleRepository
        extends JpaRepository<
        UserRoleJpaEntity,
        UserRoleJpaEntity.UserRoleId> {

    List<UserRoleJpaEntity> findByUserId(Long userId);

    void deleteByUserId(Long userId);
}