package com.movieticket.notificationservice.api.controller;

import com.movieticket.notificationservice.api.dto.request.CreateTemplateRequest;
import com.movieticket.notificationservice.api.dto.response.TemplateResponse;
import com.movieticket.notificationservice.application.command.CreateTemplateCommand;
import com.movieticket.notificationservice.application.mapper.TemplateMapper;
import com.movieticket.notificationservice.application.usecase.CreateTemplateUseCase;
import com.movieticket.notificationservice.application.usecase.ListTemplatesUseCase;
import com.movieticket.notificationservice.domain.entity.Template;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(ApiPath.TEMPLATES)
@RequiredArgsConstructor
public class TemplateController {

    private final CreateTemplateUseCase createTemplateUseCase;
    private final ListTemplatesUseCase listTemplatesUseCase;
    private final TemplateMapper templateMapper;

    @PostMapping
    public TemplateResponse create(@Valid @RequestBody CreateTemplateRequest request) {
        Template template = createTemplateUseCase.execute(
                new CreateTemplateCommand(
                        request.code(),
                        request.subject(),
                        request.body()
                )
        );

        return templateMapper.toResponse(template);
    }

    @GetMapping
    public List<TemplateResponse> list() {
        return listTemplatesUseCase.execute()
                .stream()
                .map(templateMapper::toResponse)
                .toList();
    }
}