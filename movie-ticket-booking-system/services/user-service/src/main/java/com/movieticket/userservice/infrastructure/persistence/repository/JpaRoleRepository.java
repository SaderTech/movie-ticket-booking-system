package com.movieticket.userservice.infrastructure.persistence.repository;

import com.movieticket.userservice.infrastructure.persistence.entity.RoleJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface JpaRoleRepository
        extends JpaRepository<RoleJpaEntity, Long> {

    Optional<RoleJpaEntity> findByRoleName(String roleName);

    boolean existsByRoleName(String roleName);
}