package com.movieticket.movieservice.domain.aggregate.movie;

import com.movieticket.movieservice.domain.aggregate.actor.Actor;
import com.movieticket.movieservice.domain.vo.MovieActorId;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "movie_actors")
@IdClass(MovieActorId.class)
@Getter
@NoArgsConstructor
public class MovieActor {

    @Id
    @ManyToOne
    @JoinColumn(name = "movie_id", nullable = false)
    private Movie movie;

    @Id
    @ManyToOne
    @JoinColumn(name = "actor_id", nullable = false)
    private Actor actor;

    @Column(name = "role_name", nullable = false, length = 255)
    private String roleName;

    public MovieActor(Movie movie, Actor actor, String roleName) {
        if (movie == null) {
            throw new IllegalArgumentException("Movie must not be null");
        }
        if (actor == null) {
            throw new IllegalArgumentException("Actor must not be null");
        }
        if (roleName == null || roleName.isBlank()) {
            throw new IllegalArgumentException("Actor role must not be blank");
        }
        if (roleName.trim().length() > 255) {
            throw new IllegalArgumentException("Actor role must not exceed 255 characters");
        }

        this.movie = movie;
        this.actor = actor;
        this.roleName = roleName.trim().replaceAll("\\s+", " ");
    }
}
