package com.movieticket.notificationservice.api.controller;

import com.movieticket.notificationservice.api.dto.request.CreateTemplateRequest;
import com.movieticket.notificationservice.api.dto.response.TemplateResponse;
import com.movieticket.notificationservice.application.command.CreateTemplateCommand;
import com.movieticket.notificationservice.application.usecase.CreateTemplateUseCase;
import com.movieticket.notificationservice.application.usecase.ListTemplatesUseCase;
import com.movieticket.notificationservice.domain.model.Template;
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

    @PostMapping
    public TemplateResponse create(@Valid @RequestBody CreateTemplateRequest request) {
        Template template = createTemplateUseCase.execute(
                new CreateTemplateCommand(
                        request.code(),
                        request.subject(),
                        request.body()
                )
        );

        return TemplateResponse.from(template);
    }

    @GetMapping
    public List<TemplateResponse> list() {
        return listTemplatesUseCase.execute()
                .stream()
                .map(TemplateResponse::from)
                .toList();
    }
}