package com.movieticket.movieservice.domain.aggregate.movie;

import com.movieticket.movieservice.domain.aggregate.actor.Actor;
import com.movieticket.movieservice.domain.aggregate.director.Director;
import com.movieticket.movieservice.domain.aggregate.genre.Genre;
import com.movieticket.movieservice.domain.enums.AgeRating;
import com.movieticket.movieservice.domain.enums.MovieStatus;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(
        name = "movies",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_movie_title_release_date",
                        columnNames = {"title", "release_date"}
                )
        }
)
@Getter
@NoArgsConstructor
public class Movie {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "duration_minutes", nullable = false)
    private Integer durationMinutes;

    @Column(name = "trailer_url", nullable = false, length = 500)
    private String trailerUrl;

    @Column(name = "poster_url", length = 500)
    private String posterUrl;

    @Column(name = "release_date", nullable = false)
    private LocalDate releaseDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "age_rating", nullable = false, length = 20)
    private AgeRating ageRating;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private MovieStatus status;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "movie", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MovieGenre> movieGenres = new ArrayList<>();

    @OneToMany(mappedBy = "movie", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MovieActor> movieActors = new ArrayList<>();

    @OneToMany(mappedBy = "movie", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MovieDirector> movieDirectors = new ArrayList<>();

    public Movie(
            String title,
            String description,
            Integer durationMinutes,
            String trailerUrl,
            String posterUrl,
            LocalDate releaseDate,
            AgeRating ageRating,
            MovieStatus status
    ) {
        MovieStatus effectiveStatus = status == null ? MovieStatus.COMING_SOON : status;
        validateBusinessRules(title, description, durationMinutes, trailerUrl, posterUrl,
                releaseDate, ageRating, effectiveStatus);

        this.title = normalizeRequired(title);
        this.description = normalizeNullable(description);
        this.durationMinutes = durationMinutes;
        this.trailerUrl = normalizeRequired(trailerUrl);
        this.posterUrl = normalizeNullable(posterUrl);
        this.releaseDate = releaseDate;
        this.ageRating = ageRating;
        this.status = effectiveStatus;
    }

    public void updateBasicInfo(
            String title,
            String description,
            Integer durationMinutes,
            String trailerUrl,
            String posterUrl,
            LocalDate releaseDate,
            AgeRating ageRating,
            MovieStatus status
    ) {
        MovieStatus effectiveStatus = status == null ? this.status : status;
        validateBusinessRules(title, description, durationMinutes, trailerUrl, posterUrl,
                releaseDate, ageRating, effectiveStatus);
        validateStatusTransition(effectiveStatus);

        this.title = normalizeRequired(title);
        this.description = normalizeNullable(description);
        this.durationMinutes = durationMinutes;
        this.trailerUrl = normalizeRequired(trailerUrl);
        this.posterUrl = normalizeNullable(posterUrl);
        this.releaseDate = releaseDate;
        this.ageRating = ageRating;
        this.status = effectiveStatus;
    }

    public void addGenre(Genre genre) {
        if (genre == null) {
            throw new IllegalArgumentException("Genre must not be null");
        }

        boolean exists = this.movieGenres.stream()
                .anyMatch(movieGenre -> Objects.equals(movieGenre.getGenre().getId(), genre.getId()));

        if (exists) {
            throw new IllegalArgumentException("Genre is already assigned to this movie: " + genre.getId());
        }

        this.movieGenres.add(new MovieGenre(this, genre));
    }

    public void addActor(Actor actor, String roleName) {
        if (actor == null) {
            throw new IllegalArgumentException("Actor must not be null");
        }

        boolean exists = this.movieActors.stream()
                .anyMatch(movieActor -> Objects.equals(movieActor.getActor().getId(), actor.getId()));

        if (exists) {
            throw new IllegalArgumentException("Actor is already assigned to this movie: " + actor.getId());
        }

        this.movieActors.add(new MovieActor(this, actor, roleName));
    }

    public void addDirector(Director director) {
        if (director == null) {
            throw new IllegalArgumentException("Director must not be null");
        }

        boolean exists = this.movieDirectors.stream()
                .anyMatch(movieDirector -> Objects.equals(movieDirector.getDirector().getId(), director.getId()));

        if (exists) {
            throw new IllegalArgumentException("Director is already assigned to this movie: " + director.getId());
        }

        this.movieDirectors.add(new MovieDirector(this, director));
    }

    public void clearGenres() {
        this.movieGenres.clear();
    }

    public void clearActors() {
        this.movieActors.clear();
    }

    public void clearDirectors() {
        this.movieDirectors.clear();
    }

    public void startShowing() {
        if (this.status != MovieStatus.COMING_SOON) {
            throw new IllegalStateException("Only COMING_SOON movie can start showing");
        }

        if (this.releaseDate.isAfter(LocalDate.now())) {
            throw new IllegalStateException("Movie cannot start showing before its release date");
        }

        this.status = MovieStatus.NOW_SHOWING;
    }

    public void endMovie() {
        if (this.status != MovieStatus.NOW_SHOWING) {
            throw new IllegalStateException("Only NOW_SHOWING movie can be ended");
        }

        this.status = MovieStatus.ENDED;
    }

    private void validateBusinessRules(
            String title,
            String description,
            Integer durationMinutes,
            String trailerUrl,
            String posterUrl,
            LocalDate releaseDate,
            AgeRating ageRating,
            MovieStatus status
    ) {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Movie title must not be blank");
        }

        if (title.trim().length() > 255) {
            throw new IllegalArgumentException("Movie title must not exceed 255 characters");
        }

        if (description != null && description.length() > 5000) {
            throw new IllegalArgumentException("Movie description must not exceed 5000 characters");
        }

        if (durationMinutes == null || durationMinutes <= 0 || durationMinutes > 600) {
            throw new IllegalArgumentException("Movie duration must be between 1 and 600 minutes");
        }

        if (trailerUrl == null || trailerUrl.isBlank()) {
            throw new IllegalArgumentException("Trailer URL must not be blank");
        }

        if (trailerUrl.trim().length() > 500) {
            throw new IllegalArgumentException("Trailer URL must not exceed 500 characters");
        }

        if (posterUrl != null && posterUrl.trim().length() > 500) {
            throw new IllegalArgumentException("Poster URL must not exceed 500 characters");
        }

        if (releaseDate == null) {
            throw new IllegalArgumentException("Release date must not be null");
        }

        if (ageRating == null) {
            throw new IllegalArgumentException("Age rating must not be null");
        }

        if (status == null) {
            throw new IllegalArgumentException("Movie status must not be null");
        }

        validateReleaseDateForStatus(releaseDate, status);
    }

    private void validateReleaseDateForStatus(LocalDate releaseDate, MovieStatus status) {
        if (status == MovieStatus.COMING_SOON && releaseDate.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Release date cannot be in the past for COMING_SOON movie");
        }

        if ((status == MovieStatus.NOW_SHOWING || status == MovieStatus.ENDED)
                && releaseDate.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Release date cannot be in the future for " + status + " movie");
        }
    }

    private void validateStatusTransition(MovieStatus nextStatus) {
        if (this.status == null || this.status == nextStatus) {
            return;
        }

        boolean validTransition =
                (this.status == MovieStatus.COMING_SOON && nextStatus == MovieStatus.NOW_SHOWING)
                        || (this.status == MovieStatus.NOW_SHOWING && nextStatus == MovieStatus.ENDED);

        if (!validTransition) {
            throw new IllegalStateException(
                    "Invalid movie status transition: " + this.status + " -> " + nextStatus
            );
        }
    }

    private String normalizeRequired(String value) {
        return value.trim().replaceAll("\\s+", " ");
    }

    private String normalizeNullable(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
