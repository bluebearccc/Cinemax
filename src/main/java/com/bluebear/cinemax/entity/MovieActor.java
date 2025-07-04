package com.bluebear.cinemax.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "Movie_Actor")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MovieActor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ActorID", nullable = false)
    private Actor actor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MovieID", nullable = false)
    private Movie movie;

    // Constructor without ID for creation
    public MovieActor(Actor actor, Movie movie) {
        this.actor = actor;
        this.movie = movie;
    }

    // Override equals and hashCode based on ActorID and MovieID for entity comparison
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MovieActor)) return false;

        MovieActor that = (MovieActor) o;

        if (actor != null ? !actor.getActorId().equals(that.actor != null ? that.actor.getActorId() : null) : that.actor != null)
            return false;
        return movie != null ? movie.getMovieID().equals(that.movie != null ? that.movie.getMovieID() : null) : that.movie == null;
    }

    @Override
    public int hashCode() {
        int result = actor != null ? actor.getActorId().hashCode() : 0;
        result = 31 * result + (movie != null ? movie.getMovieID().hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "MovieActor{" +
                "id=" + id +
                ", actor=" + (actor != null ? actor.getActorName() : null) +
                ", movie=" + (movie != null ? movie.getMovieName() : null) +
                '}';
    }
}