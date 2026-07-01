package com.movieticket.movieservice.application.usecase.common;

import com.movieticket.movieservice.api.dto.request.MovieActorRequest;
import com.movieticket.movieservice.api.exception.ResourceNotFoundException;
import com.movieticket.movieservice.domain.aggregate.actor.Actor;
import com.movieticket.movieservice.domain.aggregate.director.Director;
import com.movieticket.movieservice.domain.aggregate.genre.Genre;
import com.movieticket.movieservice.domain.aggregate.movie.Movie;
import com.movieticket.movieservice.infrastructure.repository.ActorRepository;
import com.movieticket.movieservice.infrastructure.repository.DirectorRepository;
import com.movieticket.movieservice.infrastructure.repository.GenreRepository;
import com.movieticket.movieservice.infrastructure.repository.MovieRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class MovieReferenceResolver {

    private final MovieRepository movieRepository;
    private final GenreRepository genreRepository;
    private final ActorRepository actorRepository;
    private final DirectorRepository directorRepository;

    public Movie findMovieById(Long id) {
        return movieRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Movie not found with id: " + id));
    }

    public Genre findGenreById(Long id) {
        return genreRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Genre not found with id: " + id));
    }

    public Actor findActorById(Long id) {
        return actorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Actor not found with id: " + id));
    }

    public Director findDirectorById(Long id) {
        return directorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Director not found with id: " + id));
    }

    public void attachGenres(Movie movie, List<Long> genreIds) {
        if (genreIds == null || genreIds.isEmpty()) {
            return;
        }

        for (Long genreId : genreIds) {
            Genre genre = findGenreById(genreId);
            movie.addGenre(genre);
        }
    }

    public void attachActors(Movie movie, List<MovieActorRequest> actors) {
        if (actors == null || actors.isEmpty()) {
            return;
        }

        for (MovieActorRequest actorRequest : actors) {
            Actor actor = findActorById(actorRequest.actorId());
            movie.addActor(actor, actorRequest.roleName());
        }
    }

    public void attachDirectors(Movie movie, List<Long> directorIds) {
        if (directorIds == null || directorIds.isEmpty()) {
            return;
        }

        for (Long directorId : directorIds) {
            Director director = findDirectorById(directorId);
            movie.addDirector(director);
        }
    }
}