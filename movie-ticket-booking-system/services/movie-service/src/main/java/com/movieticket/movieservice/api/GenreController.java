package com.movieticket.movieservice.api;

import com.movieticket.movieservice.api.dto.request.CreateGenreRequest;
import com.movieticket.movieservice.api.dto.response.GenreResponse;
import com.movieticket.movieservice.application.MovieApplicationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/genres")
public class GenreController {

    private final MovieApplicationService movieApplicationService;

    @PostMapping
    public ResponseEntity<GenreResponse> createGenre(
            @Valid @RequestBody CreateGenreRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(movieApplicationService.createGenre(request));
    }

    @GetMapping
    public ResponseEntity<List<GenreResponse>> getGenres() {
        return ResponseEntity.ok(movieApplicationService.getAllGenres());
    }
}