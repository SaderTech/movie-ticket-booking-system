package com.movieticket.movieservice.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record MovieActorRequest(

        @NotNull(message = "Actor id is required")
        Long actorId,

        @NotBlank(message = "Actor role is required")
        @Size(
                max = 255,
                message = "Actor role must not exceed 255 characters"
        )
        String roleName
) {
}
