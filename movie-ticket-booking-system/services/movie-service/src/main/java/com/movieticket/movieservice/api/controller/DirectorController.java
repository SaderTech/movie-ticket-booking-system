package com.movieticket.movieservice.api.controller;

import com.movieticket.movieservice.api.dto.request.CreateDirectorRequest;
import com.movieticket.movieservice.api.dto.request.UpdateDirectorRequest;
import com.movieticket.movieservice.api.dto.response.PersonResponse;
import com.movieticket.movieservice.application.usecase.director.CreateDirectorUseCase;
import com.movieticket.movieservice.application.usecase.director.GetAllDirectorsUseCase;
import com.movieticket.movieservice.application.usecase.director.GetDirectorByIdUseCase;
import com.movieticket.movieservice.application.usecase.director.UpdateDirectorUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/directors")
public class DirectorController {

    private final CreateDirectorUseCase createDirectorUseCase;
    private final GetAllDirectorsUseCase getAllDirectorsUseCase;
    private final GetDirectorByIdUseCase getDirectorByIdUseCase;
    private final UpdateDirectorUseCase updateDirectorUseCase;

    @PostMapping
    public ResponseEntity<PersonResponse> createDirector(
            @Valid @RequestBody CreateDirectorRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(createDirectorUseCase.execute(request));
    }

    @GetMapping
    public ResponseEntity<List<PersonResponse>> getDirectors() {
        return ResponseEntity.ok(getAllDirectorsUseCase.execute());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PersonResponse> getDirectorById(
            @PathVariable("id") Long id
    ) {
        return ResponseEntity.ok(getDirectorByIdUseCase.execute(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PersonResponse> updateDirector(
            @PathVariable("id") Long id,
            @Valid @RequestBody UpdateDirectorRequest request
    ) {
        return ResponseEntity.ok(updateDirectorUseCase.execute(id, request));
    }
}