package com.movieticket.notificationservice.infrastructure.persistence;

import com.movieticket.notificationservice.domain.entity.Template;
import com.movieticket.notificationservice.domain.repository.TemplateRepository;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class InMemoryTemplateRepositoryAdapter implements TemplateRepository {

    private final ConcurrentHashMap<UUID, Template> storage = new ConcurrentHashMap<>();

    @Override
    public Template save(Template template) {
        storage.put(template.getId(), template);
        return template;
    }

    @Override
    public Optional<Template> findById(UUID id) {
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public List<Template> findAll() {
        return storage.values()
                .stream()
                .sorted(Comparator.comparing(Template::getCode))
                .toList();
    }
}