package com.movieticket.movieservice.api.dto.response;

import com.movieticket.movieservice.domain.aggregate.movie.Movie;
import com.movieticket.movieservice.domain.enums.AgeRating;
import com.movieticket.movieservice.domain.enums.MovieStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record MovieResponse(
        Long id,
        String title,
        String description,
        Integer durationMinutes,
        String trailerUrl,
        String posterUrl,
        LocalDate releaseDate,
        AgeRating ageRating,
        MovieStatus status,
        List<GenreResponse> genres,
        List<PersonResponse> actors,
        List<PersonResponse> directors,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {

    public static MovieResponse from(Movie movie) {
        List<GenreResponse> genres = movie.getMovieGenres()
                .stream()
                .map(movieGenre -> new GenreResponse(
                        movieGenre.getGenre().getId(),
                        movieGenre.getGenre().getName(),
                        movieGenre.getGenre().getDescription()
                ))
                .toList();

        List<PersonResponse> actors = movie.getMovieActors()
                .stream()
                .map(movieActor -> new PersonResponse(
                        movieActor.getActor().getId(),
                        movieActor.getActor().getName(),
                        movieActor.getActor().getAvatarUrl(),
                        movieActor.getActor().getBiography(),
                        movieActor.getActor().getBirthDate(),
                        movieActor.getRoleName()
                ))
                .toList();

        List<PersonResponse> directors = movie.getMovieDirectors()
                .stream()
                .map(movieDirector -> new PersonResponse(
                        movieDirector.getDirector().getId(),
                        movieDirector.getDirector().getName(),
                        null,
                        movieDirector.getDirector().getBiography(),
                        movieDirector.getDirector().getBirthDate(),
                        null
                ))
                .toList();

        return new MovieResponse(
                movie.getId(),
                movie.getTitle(),
                movie.getDescription(),
                movie.getDurationMinutes(),
                movie.getTrailerUrl(),
                movie.getPosterUrl(),
                movie.getReleaseDate(),
                movie.getAgeRating(),
                movie.getStatus(),
                genres,
                actors,
                directors,
                movie.getCreatedAt(),
                movie.getUpdatedAt()
        );
    }
}