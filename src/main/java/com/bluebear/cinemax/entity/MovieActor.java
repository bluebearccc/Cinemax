package com.bluebear.cinemax.entity;


import jakarta.persistence.*;



@Entity
@Table(name = "Movie_Actor")
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

    // Constructors
    public MovieActor() {}

    public MovieActor(Actor actor, Movie movie) {
        this.actor = actor;
        this.movie = movie;
    }

    // Getters and Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Actor getActor() {
        return actor;
    }

    public void setActor(Actor actor) {
        this.actor = actor;
    }

    public Movie getMovie() {
        return movie;
    }

    public void setMovie(Movie movie) {
        this.movie = movie;
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
