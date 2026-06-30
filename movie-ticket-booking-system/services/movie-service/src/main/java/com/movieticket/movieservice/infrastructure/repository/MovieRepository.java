package com.movieticket.movieservice.infrastructure.repository;

import com.movieticket.movieservice.domain.aggregate.movie.Movie;
import com.movieticket.movieservice.domain.enums.MovieStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MovieRepository extends JpaRepository<Movie, Long> {

    boolean existsByTitleIgnoreCase(String title);

    List<Movie> findByStatus(MovieStatus status);

    List<Movie> findByTitleContainingIgnoreCase(String title);
}