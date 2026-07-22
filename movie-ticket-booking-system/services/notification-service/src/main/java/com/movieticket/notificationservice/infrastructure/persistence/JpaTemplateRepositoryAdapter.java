package com.movieticket.notificationservice.infrastructure.persistence;

import com.movieticket.notificationservice.domain.entity.Template;
import com.movieticket.notificationservice.domain.repository.TemplateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@Primary
@RequiredArgsConstructor
public class JpaTemplateRepositoryAdapter implements TemplateRepository {

    private final SpringDataTemplateJpaRepository repository;

    @Override
    public Template save(Template template) {
        return repository.save(JpaTemplateEntity.fromDomain(template)).toDomain();
    }

    @Override
    public Optional<Template> findById(UUID id) {
        return repository.findById(id).map(JpaTemplateEntity::toDomain);
    }

    @Override
    public List<Template> findAll() {
        return repository.findAll()
                .stream()
                .map(JpaTemplateEntity::toDomain)
                .sorted(Comparator.comparing(Template::getCode))
                .toList();
    }
}
