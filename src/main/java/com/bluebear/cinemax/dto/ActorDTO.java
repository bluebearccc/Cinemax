package com.bluebear.cinemax.dto;

import java.util.List;

public class ActorDTO {
    private Integer actorId;
    private String actorName;
    private String image;
    private List<String> movies;

    // Constructors
    public ActorDTO() {}

    public ActorDTO(Integer actorId, String actorName, String image) {
        this.actorId = actorId;
        this.actorName = actorName;
        this.image = image;
    }

    public ActorDTO(Integer actorId, String actorName, String image, List<String> movies) {
        this.actorId = actorId;
        this.actorName = actorName;
        this.image = image;
        this.movies = movies;
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

    public List<String> getMovies() {
        return movies;
    }

    public void setMovies(List<String> movies) {
        this.movies = movies;
    }

    @Override
    public String toString() {
        return "ActorDTO{" +
                "actorId=" + actorId +
                ", actorName='" + actorName + '\'' +
                ", image='" + image + '\'' +
                ", movies=" + movies +
                '}';
    }
}