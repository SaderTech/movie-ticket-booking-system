package com.movieticket.userservice.infrastructure.persistence;

import com.movieticket.userservice.infrastructure.persistence.entity.UserProfileJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface JpaUserProfileRepository
        extends JpaRepository<UserProfileJpaEntity, Long> {

    Optional<UserProfileJpaEntity> findByUserId(Long userId);
}