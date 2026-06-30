package com.movieticket.movieservice.api;

import com.movieticket.movieservice.api.dto.request.CreateMovieRequest;
import com.movieticket.movieservice.api.dto.request.UpdateMovieRequest;
import com.movieticket.movieservice.api.dto.response.MovieResponse;
import com.movieticket.movieservice.application.usecase.movie.CreateMovieUseCase;
import com.movieticket.movieservice.application.usecase.movie.EndMovieUseCase;
import com.movieticket.movieservice.application.usecase.movie.GetAllMoviesUseCase;
import com.movieticket.movieservice.application.usecase.movie.GetComingSoonMoviesUseCase;
import com.movieticket.movieservice.application.usecase.movie.GetMovieByIdUseCase;
import com.movieticket.movieservice.application.usecase.movie.GetMoviesByStatusUseCase;
import com.movieticket.movieservice.application.usecase.movie.GetNowShowingMoviesUseCase;
import com.movieticket.movieservice.application.usecase.movie.SearchMoviesByTitleUseCase;
import com.movieticket.movieservice.application.usecase.movie.UpdateMovieUseCase;
import com.movieticket.movieservice.domain.enums.MovieStatus;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/movies")
public class MovieController {

    private final CreateMovieUseCase createMovieUseCase;
    private final GetMovieByIdUseCase getMovieByIdUseCase;
    private final GetAllMoviesUseCase getAllMoviesUseCase;
    private final GetMoviesByStatusUseCase getMoviesByStatusUseCase;
    private final SearchMoviesByTitleUseCase searchMoviesByTitleUseCase;
    private final GetNowShowingMoviesUseCase getNowShowingMoviesUseCase;
    private final GetComingSoonMoviesUseCase getComingSoonMoviesUseCase;
    private final UpdateMovieUseCase updateMovieUseCase;
    private final EndMovieUseCase endMovieUseCase;

    @PostMapping
    public ResponseEntity<MovieResponse> createMovie(
            @Valid @RequestBody CreateMovieRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(createMovieUseCase.execute(request));
    }

    @GetMapping
    public ResponseEntity<List<MovieResponse>> getMovies(
            @RequestParam(name = "status", required = false) MovieStatus status
    ) {
        if (status != null) {
            return ResponseEntity.ok(getMoviesByStatusUseCase.execute(status));
        }

        return ResponseEntity.ok(getAllMoviesUseCase.execute());
    }

    @GetMapping("/search")
    public ResponseEntity<List<MovieResponse>> searchMovies(
            @RequestParam(name = "keyword", required = false) String keyword
    ) {
        return ResponseEntity.ok(searchMoviesByTitleUseCase.execute(keyword));
    }

    @GetMapping("/now-showing")
    public ResponseEntity<List<MovieResponse>> getNowShowingMovies() {
        return ResponseEntity.ok(getNowShowingMoviesUseCase.execute());
    }

    @GetMapping("/coming-soon")
    public ResponseEntity<List<MovieResponse>> getComingSoonMovies() {
        return ResponseEntity.ok(getComingSoonMoviesUseCase.execute());
    }

    @GetMapping("/{id}")
    public ResponseEntity<MovieResponse> getMovieById(
            @PathVariable("id") Long id
    ) {
        return ResponseEntity.ok(getMovieByIdUseCase.execute(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<MovieResponse> updateMovie(
            @PathVariable("id") Long id,
            @Valid @RequestBody UpdateMovieRequest request
    ) {
        return ResponseEntity.ok(updateMovieUseCase.execute(id, request));
    }

    @PatchMapping("/{id}/end")
    public ResponseEntity<MovieResponse> endMovie(
            @PathVariable("id") Long id
    ) {
        return ResponseEntity.ok(endMovieUseCase.execute(id));
    }

    /*
     * Không dùng hard delete Movie vì sau này Showtime Service sẽ tham chiếu Movie.id.
     * Thay vào đó dùng PATCH /api/movies/{id}/end để chuyển status sang ENDED.
     */

    @GetMapping("/demo-receive")
    public String receiveDemo(HttpServletRequest request) {
        String correlationId = request.getHeader("X-Correlation-ID");
        String userId = request.getHeader("X-User-ID");
        String userEmail = request.getHeader("X-User-Email");

        log.info("============== KẾT QUẢ PROPAGATION ==============");
        log.info("=> [2. Movie Service] Nhận lệnh Feign thành công!");
        log.info("=> Mã định danh hệ thống (Correlation ID): {}", correlationId);
        log.info("=> Mã người dùng (User ID): {}", userId);
        log.info("=> Email người dùng: {}", userEmail);
        log.info("=================================================");

        return "[Movie Service phản hồi] Đã nhận được mã: " + correlationId;
    }
}