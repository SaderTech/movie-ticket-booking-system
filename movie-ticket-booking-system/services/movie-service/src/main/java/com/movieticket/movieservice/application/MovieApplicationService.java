package com.movieticket.movieservice.application;

import com.movieticket.movieservice.api.dto.request.CreateActorRequest;
import com.movieticket.movieservice.api.dto.request.CreateDirectorRequest;
import com.movieticket.movieservice.api.dto.request.CreateGenreRequest;
import com.movieticket.movieservice.api.dto.request.CreateMovieRequest;
import com.movieticket.movieservice.api.dto.request.MovieActorRequest;
import com.movieticket.movieservice.api.dto.request.UpdateMovieRequest;
import com.movieticket.movieservice.api.dto.response.GenreResponse;
import com.movieticket.movieservice.api.dto.response.MovieResponse;
import com.movieticket.movieservice.api.dto.response.PersonResponse;
import com.movieticket.movieservice.api.exception.BusinessException;
import com.movieticket.movieservice.api.exception.ResourceNotFoundException;
import com.movieticket.movieservice.domain.aggregate.actor.Actor;
import com.movieticket.movieservice.domain.aggregate.director.Director;
import com.movieticket.movieservice.domain.aggregate.genre.Genre;
import com.movieticket.movieservice.domain.aggregate.movie.Movie;
import com.movieticket.movieservice.domain.enums.MovieStatus;
import com.movieticket.movieservice.infrastructure.repository.ActorRepository;
import com.movieticket.movieservice.infrastructure.repository.DirectorRepository;
import com.movieticket.movieservice.infrastructure.repository.GenreRepository;
import com.movieticket.movieservice.infrastructure.repository.MovieRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class MovieApplicationService {

    private final MovieRepository movieRepository;
    private final GenreRepository genreRepository;
    private final ActorRepository actorRepository;
    private final DirectorRepository directorRepository;

    public GenreResponse createGenre(CreateGenreRequest request) {
        if (genreRepository.existsByNameIgnoreCase(request.name())) {
            throw new BusinessException("Genre name already exists: " + request.name());
        }

        Genre genre = new Genre(request.name(), request.description());
        Genre savedGenre = genreRepository.save(genre);

        return new GenreResponse(
                savedGenre.getId(),
                savedGenre.getName(),
                savedGenre.getDescription()
        );
    }

    @Transactional(readOnly = true)
    public List<GenreResponse> getAllGenres() {
        return genreRepository.findAll()
                .stream()
                .map(genre -> new GenreResponse(
                        genre.getId(),
                        genre.getName(),
                        genre.getDescription()
                ))
                .toList();
    }

    public PersonResponse createActor(CreateActorRequest request) {
        Actor actor = new Actor(
                request.name(),
                request.avatarUrl(),
                request.biography(),
                request.birthDate()
        );

        Actor savedActor = actorRepository.save(actor);

        return new PersonResponse(
                savedActor.getId(),
                savedActor.getName(),
                savedActor.getAvatarUrl(),
                savedActor.getBiography(),
                savedActor.getBirthDate(),
                null
        );
    }

    @Transactional(readOnly = true)
    public List<PersonResponse> getAllActors() {
        return actorRepository.findAll()
                .stream()
                .map(actor -> new PersonResponse(
                        actor.getId(),
                        actor.getName(),
                        actor.getAvatarUrl(),
                        actor.getBiography(),
                        actor.getBirthDate(),
                        null
                ))
                .toList();
    }

    public PersonResponse createDirector(CreateDirectorRequest request) {
        Director director = new Director(
                request.name(),
                request.biography(),
                request.birthDate()
        );

        Director savedDirector = directorRepository.save(director);

        return new PersonResponse(
                savedDirector.getId(),
                savedDirector.getName(),
                null,
                savedDirector.getBiography(),
                savedDirector.getBirthDate(),
                null
        );
    }

    @Transactional(readOnly = true)
    public List<PersonResponse> getAllDirectors() {
        return directorRepository.findAll()
                .stream()
                .map(director -> new PersonResponse(
                        director.getId(),
                        director.getName(),
                        null,
                        director.getBiography(),
                        director.getBirthDate(),
                        null
                ))
                .toList();
    }

    public MovieResponse createMovie(CreateMovieRequest request) {
        if (movieRepository.existsByTitleIgnoreCase(request.title())) {
            throw new BusinessException("Movie title already exists: " + request.title());
        }

        Movie movie = new Movie(
                request.title(),
                request.description(),
                request.durationMinutes(),
                request.trailerUrl(),
                request.posterUrl(),
                request.releaseDate(),
                request.ageRating(),
                request.status()
        );

        attachGenres(movie, request.genreIds());
        attachActors(movie, request.actors());
        attachDirectors(movie, request.directorIds());

        Movie savedMovie = movieRepository.save(movie);

        return MovieResponse.from(savedMovie);
    }

    @Transactional(readOnly = true)
    public MovieResponse getMovieById(Long id) {
        Movie movie = findMovieById(id);
        return MovieResponse.from(movie);
    }

    @Transactional(readOnly = true)
    public List<MovieResponse> getAllMovies() {
        return movieRepository.findAll()
                .stream()
                .map(MovieResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<MovieResponse> getMoviesByStatus(MovieStatus status) {
        return movieRepository.findByStatus(status)
                .stream()
                .map(MovieResponse::from)
                .toList();
    }

    public MovieResponse updateMovie(Long id, UpdateMovieRequest request) {
        Movie movie = findMovieById(id);

        movie.updateBasicInfo(
                request.title(),
                request.description(),
                request.durationMinutes(),
                request.trailerUrl(),
                request.posterUrl(),
                request.releaseDate(),
                request.ageRating(),
                request.status()
        );

        movie.clearGenres();
        movie.clearActors();
        movie.clearDirectors();

        attachGenres(movie, request.genreIds());
        attachActors(movie, request.actors());
        attachDirectors(movie, request.directorIds());

        Movie savedMovie = movieRepository.save(movie);

        return MovieResponse.from(savedMovie);
    }

    public MovieResponse endMovie(Long id) {
        Movie movie = findMovieById(id);
        movie.endMovie();

        Movie savedMovie = movieRepository.save(movie);
        return MovieResponse.from(savedMovie);
    }

    private Movie findMovieById(Long id) {
        return movieRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Movie not found with id: " + id));
    }

    private Genre findGenreById(Long id) {
        return genreRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Genre not found with id: " + id));
    }

    private Actor findActorById(Long id) {
        return actorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Actor not found with id: " + id));
    }

    private Director findDirectorById(Long id) {
        return directorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Director not found with id: " + id));
    }

    private void attachGenres(Movie movie, List<Long> genreIds) {
        if (genreIds == null || genreIds.isEmpty()) {
            return;
        }

        for (Long genreId : genreIds) {
            Genre genre = findGenreById(genreId);
            movie.addGenre(genre);
        }
    }

    private void attachActors(Movie movie, List<MovieActorRequest> actors) {
        if (actors == null || actors.isEmpty()) {
            return;
        }

        for (MovieActorRequest actorRequest : actors) {
            Actor actor = findActorById(actorRequest.actorId());
            movie.addActor(actor, actorRequest.roleName());
        }
    }

    private void attachDirectors(Movie movie, List<Long> directorIds) {
        if (directorIds == null || directorIds.isEmpty()) {
            return;
        }

        for (Long directorId : directorIds) {
            Director director = findDirectorById(directorId);
            movie.addDirector(director);
        }
    }
}