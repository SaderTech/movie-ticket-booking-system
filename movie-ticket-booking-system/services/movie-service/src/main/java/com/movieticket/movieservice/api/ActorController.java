package com.movieticket.movieservice.api;

import com.movieticket.movieservice.api.dto.request.CreateActorRequest;
import com.movieticket.movieservice.api.dto.request.UpdateActorRequest;
import com.movieticket.movieservice.api.dto.response.PersonResponse;
import com.movieticket.movieservice.application.usecase.actor.CreateActorUseCase;
import com.movieticket.movieservice.application.usecase.actor.GetActorByIdUseCase;
import com.movieticket.movieservice.application.usecase.actor.GetAllActorsUseCase;
import com.movieticket.movieservice.application.usecase.actor.UpdateActorUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/actors")
public class ActorController {

    private final CreateActorUseCase createActorUseCase;
    private final GetAllActorsUseCase getAllActorsUseCase;
    private final GetActorByIdUseCase getActorByIdUseCase;
    private final UpdateActorUseCase updateActorUseCase;

    @PostMapping
    public ResponseEntity<PersonResponse> createActor(
            @Valid @RequestBody CreateActorRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(createActorUseCase.execute(request));
    }

    @GetMapping
    public ResponseEntity<List<PersonResponse>> getActors() {
        return ResponseEntity.ok(getAllActorsUseCase.execute());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PersonResponse> getActorById(
            @PathVariable("id") Long id
    ) {
        return ResponseEntity.ok(getActorByIdUseCase.execute(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PersonResponse> updateActor(
            @PathVariable("id") Long id,
            @Valid @RequestBody UpdateActorRequest request
    ) {
        return ResponseEntity.ok(updateActorUseCase.execute(id, request));
    }
}