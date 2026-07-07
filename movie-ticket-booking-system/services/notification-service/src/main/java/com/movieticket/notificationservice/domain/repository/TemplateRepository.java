package com.movieticket.notificationservice.domain.repository;

import com.movieticket.notificationservice.domain.entity.Template;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TemplateRepository {

    Template save(Template template);

    Optional<Template> findById(UUID id);

    List<Template> findAll();
}