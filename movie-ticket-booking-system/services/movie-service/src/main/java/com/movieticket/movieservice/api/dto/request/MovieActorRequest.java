package com.movieticket.movieservice.api.dto.request;

import jakarta.validation.constraints.NotNull;

public record MovieActorRequest(
        @NotNull(message = "Actor id is required")
        Long actorId,

        String roleName
) {
}