package com.movieticket.movieservice.domain.aggregate.movie;

import com.movieticket.movieservice.domain.aggregate.director.Director;
import com.movieticket.movieservice.domain.vo.MovieDirectorId;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "movie_directors")
@IdClass(MovieDirectorId.class)
@Getter
@NoArgsConstructor
public class MovieDirector {

    @Id
    @ManyToOne
    @JoinColumn(name = "movie_id", nullable = false)
    private Movie movie;

    @Id
    @ManyToOne
    @JoinColumn(name = "director_id", nullable = false)
    private Director director;

    public MovieDirector(Movie movie, Director director) {
        if (movie == null) {
            throw new IllegalArgumentException("Movie must not be null");
        }
        if (director == null) {
            throw new IllegalArgumentException("Director must not be null");
        }
        this.movie = movie;
        this.director = director;
    }
}
