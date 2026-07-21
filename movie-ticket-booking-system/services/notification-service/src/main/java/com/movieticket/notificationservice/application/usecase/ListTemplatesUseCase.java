package com.movieticket.notificationservice.application.usecase;

import com.movieticket.notificationservice.domain.entity.Template;
import com.movieticket.notificationservice.domain.repository.TemplateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ListTemplatesUseCase {

    private final TemplateRepository templateRepository;

    public List<Template> execute() {
        return templateRepository.findAll();
    }
}