package com.bluebear.cinemax.entity;
import jakarta.persistence.*;
import java.util.Set;

@Entity
@Table(name = "Actor")
public class Actor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ActorID")
    private Integer actorId;

    @Column(name = "ActorName", nullable = false)
    private String actorName;

    @Column(name = "Image", nullable = false)
    private String image;

    @OneToMany(mappedBy = "actor", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<MovieActor> movieActors;

    // Constructors
    public Actor() {}

    public Actor(String actorName, String image) {
        this.actorName = actorName;
        this.image = image;
    }

    // Getters and Setters
    public Integer getActorId() {
        return actorId;
    }

    public void setActorId(Integer actorId) {
        this.actorId = actorId;
    }

    public String getActorName() {
        return actorName;
    }

    public void setActorName(String actorName) {
        this.actorName = actorName;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public Set<MovieActor> getMovieActors() {
        return movieActors;
    }

    public void setMovieActors(Set<MovieActor> movieActors) {
        this.movieActors = movieActors;
    }

    @Override
    public String toString() {
        return "Actor{" +
                "actorId=" + actorId +
                ", actorName='" + actorName + '\'' +
                ", image='" + image + '\'' +
                '}';
    }
}
