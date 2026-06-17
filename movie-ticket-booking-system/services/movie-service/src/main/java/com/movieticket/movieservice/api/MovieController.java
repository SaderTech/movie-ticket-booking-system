package com.movieticket.movieservice.api;

import com.movieticket.movieservice.api.dto.request.CreateMovieRequest;
import com.movieticket.movieservice.api.dto.request.UpdateMovieRequest;
import com.movieticket.movieservice.api.dto.response.MovieResponse;
import com.movieticket.movieservice.application.MovieApplicationService;
import com.movieticket.movieservice.domain.enums.MovieStatus;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/movies")
public class MovieController {

    private final MovieApplicationService movieApplicationService;

    @PostMapping
    public ResponseEntity<MovieResponse> createMovie(
            @Valid @RequestBody CreateMovieRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(movieApplicationService.createMovie(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<MovieResponse> getMovieById(@PathVariable Long id) {
        return ResponseEntity.ok(movieApplicationService.getMovieById(id));
    }

    @GetMapping
    public ResponseEntity<List<MovieResponse>> getMovies(
            @RequestParam(required = false) MovieStatus status
    ) {
        if (status != null) {
            return ResponseEntity.ok(movieApplicationService.getMoviesByStatus(status));
        }

        return ResponseEntity.ok(movieApplicationService.getAllMovies());
    }

    @PutMapping("/{id}")
    public ResponseEntity<MovieResponse> updateMovie(
            @PathVariable Long id,
            @Valid @RequestBody UpdateMovieRequest request
    ) {
        return ResponseEntity.ok(movieApplicationService.updateMovie(id, request));
    }

    @PatchMapping("/{id}/end")
    public ResponseEntity<MovieResponse> endMovie(@PathVariable Long id) {
        return ResponseEntity.ok(movieApplicationService.endMovie(id));
    }

    /*
     * Không dùng hard delete Movie vì sau này Showtime Service sẽ tham chiếu Movie.id.
     * Thay vào đó dùng PATCH /api/movies/{id}/end để chuyển status sang ENDED.
     */
}