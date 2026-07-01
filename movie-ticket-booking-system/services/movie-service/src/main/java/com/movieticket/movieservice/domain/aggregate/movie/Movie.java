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
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "movies")
@Getter
@Setter
@NoArgsConstructor
public class Movie {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "duration_minutes", nullable = false)
    private Integer durationMinutes;

    @Column(name = "trailer_url", length = 500)
    private String trailerUrl;

    @Column(name = "poster_url", length = 500)
    private String posterUrl;

    @Column(name = "release_date")
    private LocalDate releaseDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "age_rating", length = 20)
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
        validateTitle(title);
        validateDuration(durationMinutes);

        this.title = title;
        this.description = description;
        this.durationMinutes = durationMinutes;
        this.trailerUrl = trailerUrl;
        this.posterUrl = posterUrl;
        this.releaseDate = releaseDate;
        this.ageRating = ageRating;
        this.status = status == null ? MovieStatus.COMING_SOON : status;

        validateReleaseDateForComingSoon();
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
        validateTitle(title);
        validateDuration(durationMinutes);

        this.title = title;
        this.description = description;
        this.durationMinutes = durationMinutes;
        this.trailerUrl = trailerUrl;
        this.posterUrl = posterUrl;
        this.releaseDate = releaseDate;
        this.ageRating = ageRating;
        this.status = status == null ? MovieStatus.COMING_SOON : status;

        validateReleaseDateForComingSoon();
    }

    public void addGenre(Genre genre) {
        if (genre == null) {
            throw new IllegalArgumentException("Genre must not be null");
        }

        boolean exists = this.movieGenres.stream()
                .anyMatch(movieGenre -> movieGenre.getGenre().getId().equals(genre.getId()));

        if (!exists) {
            this.movieGenres.add(new MovieGenre(this, genre));
        }
    }

    public void addActor(Actor actor, String roleName) {
        if (actor == null) {
            throw new IllegalArgumentException("Actor must not be null");
        }

        boolean exists = this.movieActors.stream()
                .anyMatch(movieActor -> movieActor.getActor().getId().equals(actor.getId()));

        if (!exists) {
            this.movieActors.add(new MovieActor(this, actor, roleName));
        }
    }

    public void addDirector(Director director) {
        if (director == null) {
            throw new IllegalArgumentException("Director must not be null");
        }

        boolean exists = this.movieDirectors.stream()
                .anyMatch(movieDirector -> movieDirector.getDirector().getId().equals(director.getId()));

        if (!exists) {
            this.movieDirectors.add(new MovieDirector(this, director));
        }
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

    public void endMovie() {
        this.status = MovieStatus.ENDED;
    }

    private void validateTitle(String title) {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Movie title must not be blank");
        }
    }

    private void validateDuration(Integer durationMinutes) {
        if (durationMinutes == null || durationMinutes <= 0) {
            throw new IllegalArgumentException("Movie duration must be greater than 0");
        }
    }

    private void validateReleaseDateForComingSoon() {
        if (this.status == MovieStatus.COMING_SOON
                && this.releaseDate != null
                && this.releaseDate.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Release date cannot be in the past for COMING_SOON movie");
        }
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