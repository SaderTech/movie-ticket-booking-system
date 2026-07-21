package com.movieticket.movieservice.application.service;

import com.movieticket.movieservice.application.dto.request.MovieActorRequest;
import com.movieticket.movieservice.application.exception.ResourceNotFoundException;
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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
            throw new IllegalArgumentException("Movie must have at least one genre");
        }

        validateDistinctIds(genreIds, "Genre");

        for (Long genreId : genreIds) {
            Genre genre = findGenreById(genreId);
            movie.addGenre(genre);
        }
    }

    public void attachActors(Movie movie, List<MovieActorRequest> actors) {
        if (actors == null || actors.isEmpty()) {
            throw new IllegalArgumentException("Movie must have at least one actor");
        }

        List<Long> actorIds = actors.stream()
                .map(MovieActorRequest::actorId)
                .toList();
        validateDistinctIds(actorIds, "Actor");

        for (MovieActorRequest actorRequest : actors) {
            Actor actor = findActorById(actorRequest.actorId());
            movie.addActor(actor, actorRequest.roleName());
        }
    }

    public void attachDirectors(Movie movie, List<Long> directorIds) {
        if (directorIds == null || directorIds.isEmpty()) {
            throw new IllegalArgumentException("Movie must have at least one director");
        }

        validateDistinctIds(directorIds, "Director");

        for (Long directorId : directorIds) {
            Director director = findDirectorById(directorId);
            movie.addDirector(director);
        }
    }

    private void validateDistinctIds(List<Long> ids, String resourceName) {
        if (ids.stream().anyMatch(id -> id == null)) {
            throw new IllegalArgumentException(resourceName + " id must not be null");
        }

        Set<Long> distinctIds = new HashSet<>(ids);
        if (distinctIds.size() != ids.size()) {
            throw new IllegalArgumentException(resourceName + " list contains duplicate ids");
        }
    }
}
