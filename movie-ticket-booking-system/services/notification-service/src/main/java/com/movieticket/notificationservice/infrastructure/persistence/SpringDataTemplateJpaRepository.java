package com.movieticket.notificationservice.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface SpringDataTemplateJpaRepository extends JpaRepository<JpaTemplateEntity, UUID> {

    Optional<JpaTemplateEntity> findByCode(String code);
}
