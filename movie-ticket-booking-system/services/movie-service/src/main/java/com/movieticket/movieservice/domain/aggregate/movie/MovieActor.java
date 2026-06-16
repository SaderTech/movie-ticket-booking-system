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
import lombok.Setter;

@Entity
@Table(name = "movie_actors")
@IdClass(MovieActorId.class)
@Getter
@Setter
@NoArgsConstructor
public class MovieActor {

    @Id
    @ManyToOne
    @JoinColumn(name = "movie_id")
    private Movie movie;

    @Id
    @ManyToOne
    @JoinColumn(name = "actor_id")
    private Actor actor;

    @Column(name = "role_name")
    private String roleName;

    public MovieActor(Movie movie, Actor actor, String roleName) {
        this.movie = movie;
        this.actor = actor;
        this.roleName = roleName;
    }
}