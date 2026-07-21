package com.movieticket.movieservice.domain.aggregate.movie;

import com.movieticket.movieservice.domain.aggregate.genre.Genre;
import com.movieticket.movieservice.domain.vo.MovieGenreId;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "movie_genres")
@IdClass(MovieGenreId.class)
@Getter
@NoArgsConstructor
public class MovieGenre {

    @Id
    @ManyToOne
    @JoinColumn(name = "movie_id", nullable = false)
    private Movie movie;

    @Id
    @ManyToOne
    @JoinColumn(name = "genre_id", nullable = false)
    private Genre genre;

    public MovieGenre(Movie movie, Genre genre) {
        if (movie == null) {
            throw new IllegalArgumentException("Movie must not be null");
        }
        if (genre == null) {
            throw new IllegalArgumentException("Genre must not be null");
        }
        this.movie = movie;
        this.genre = genre;
    }
}
