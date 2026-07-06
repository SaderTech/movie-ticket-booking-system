package com.movieticket.notificationservice.application.mapper;

import com.movieticket.notificationservice.api.dto.response.TemplateResponse;
import com.movieticket.notificationservice.domain.entity.Template;
import org.springframework.stereotype.Component;

@Component
public class TemplateMapper {

    public TemplateResponse toResponse(Template template) {
        return TemplateResponse.from(template);
    }
}
