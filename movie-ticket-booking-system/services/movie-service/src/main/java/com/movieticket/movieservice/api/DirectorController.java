package com.movieticket.movieservice.api;

import com.movieticket.movieservice.api.dto.request.CreateDirectorRequest;
import com.movieticket.movieservice.api.dto.response.PersonResponse;
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
@RequestMapping("/api/directors")
public class DirectorController {

    private final MovieApplicationService movieApplicationService;

    @PostMapping
    public ResponseEntity<PersonResponse> createDirector(
            @Valid @RequestBody CreateDirectorRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(movieApplicationService.createDirector(request));
    }

    @GetMapping
    public ResponseEntity<List<PersonResponse>> getDirectors() {
        return ResponseEntity.ok(movieApplicationService.getAllDirectors());
    }
}