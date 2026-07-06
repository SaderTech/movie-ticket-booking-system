package com.movieticket.notificationservice.application.usecase;

import com.movieticket.notificationservice.application.command.CreateTemplateCommand;
import com.movieticket.notificationservice.domain.model.Template;
import com.movieticket.notificationservice.domain.repository.TemplateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CreateTemplateUseCase {

    private final TemplateRepository templateRepository;

    public Template execute(CreateTemplateCommand command) {
        Template template = Template.create(
                command.code(),
                command.subject(),
                command.body()
        );

        return templateRepository.save(template);
    }
}