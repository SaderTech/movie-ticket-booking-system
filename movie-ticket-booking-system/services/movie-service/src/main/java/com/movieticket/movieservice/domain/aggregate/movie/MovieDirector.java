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
import lombok.Setter;

@Entity
@Table(name = "movie_directors")
@IdClass(MovieDirectorId.class)
@Getter
@Setter
@NoArgsConstructor
public class MovieDirector {

    @Id
    @ManyToOne
    @JoinColumn(name = "movie_id")
    private Movie movie;

    @Id
    @ManyToOne
    @JoinColumn(name = "director_id")
    private Director director;

    public MovieDirector(Movie movie, Director director) {
        this.movie = movie;
        this.director = director;
    }
}