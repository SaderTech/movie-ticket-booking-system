package com.movieticket.userservice.infrastructure.persistence.repository;

import com.movieticket.userservice.infrastructure.persistence.entity.UserJpaEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface JpaUserRepository
        extends JpaRepository<UserJpaEntity, Long> {


    Optional<UserJpaEntity> findByUsername(String username);


    Optional<UserJpaEntity> findByEmail(String email);


    boolean existsByUsername(String username);


    boolean existsByEmail(String email);


    // ===============================
    // SEARCH USER
    // username OR email OR fullName
    // ===============================
    Page<UserJpaEntity>
    findByUsernameContainingIgnoreCaseOrEmailContainingIgnoreCaseOrFullNameContainingIgnoreCase(
            String username,
            String email,
            String fullName,
            Pageable pageable
    );

}