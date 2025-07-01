package com.bluebear.cinemax.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.Set;

@Entity
@Table(name = "Actor")
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"movieActors"}) // Avoid circular reference in toString
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

    // Constructor without movieActors for basic creation
    public Actor(String actorName, String image) {
        this.actorName = actorName;
        this.image = image;
    }
}