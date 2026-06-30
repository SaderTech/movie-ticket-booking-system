package com.movieticket.movieservice.api;

import com.movieticket.movieservice.api.dto.request.CreateGenreRequest;
import com.movieticket.movieservice.api.dto.request.UpdateGenreRequest;
import com.movieticket.movieservice.api.dto.response.GenreResponse;
import com.movieticket.movieservice.application.usecase.genre.CreateGenreUseCase;
import com.movieticket.movieservice.application.usecase.genre.GetAllGenresUseCase;
import com.movieticket.movieservice.application.usecase.genre.GetGenreByIdUseCase;
import com.movieticket.movieservice.application.usecase.genre.UpdateGenreUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/genres")
public class GenreController {

    private final CreateGenreUseCase createGenreUseCase;
    private final GetAllGenresUseCase getAllGenresUseCase;
    private final GetGenreByIdUseCase getGenreByIdUseCase;
    private final UpdateGenreUseCase updateGenreUseCase;

    @PostMapping
    public ResponseEntity<GenreResponse> createGenre(
            @Valid @RequestBody CreateGenreRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(createGenreUseCase.execute(request));
    }

    @GetMapping
    public ResponseEntity<List<GenreResponse>> getGenres() {
        return ResponseEntity.ok(getAllGenresUseCase.execute());
    }

    @GetMapping("/{id}")
    public ResponseEntity<GenreResponse> getGenreById(
            @PathVariable("id") Long id
    ) {
        return ResponseEntity.ok(getGenreByIdUseCase.execute(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<GenreResponse> updateGenre(
            @PathVariable("id") Long id,
            @Valid @RequestBody UpdateGenreRequest request
    ) {
        return ResponseEntity.ok(updateGenreUseCase.execute(id, request));
    }
}