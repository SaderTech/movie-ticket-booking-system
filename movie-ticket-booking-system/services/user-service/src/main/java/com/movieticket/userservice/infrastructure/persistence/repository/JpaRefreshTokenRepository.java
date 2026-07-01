package com.movieticket.userservice.infrastructure.persistence.repository;

import com.movieticket.userservice.infrastructure.persistence.entity.RefreshTokenJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface JpaRefreshTokenRepository
        extends JpaRepository<RefreshTokenJpaEntity, Long> {

    Optional<RefreshTokenJpaEntity> findByToken(String token);

    List<RefreshTokenJpaEntity> findByUserId(Long userId);

    void deleteByUserId(Long userId);

    void deleteByToken(String token);
}