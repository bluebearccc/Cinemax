package com.bluebear.cinemax.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import java.util.List;

@Entity
@Table(name = "Actor")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Actor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ActorID")
    private Integer actorID;

    @Column(name = "ActorName", nullable = false, length = 255)
    private String actorName;

    @Column(name = "Image", nullable = false, length = 255)
    private String image;

    // UPDATED: Quan hệ ngược với Movie sử dụng @ManyToMany
    @JsonIgnore
    @ManyToMany(mappedBy = "actors", fetch = FetchType.LAZY)
    private List<Movie> movies;

    public Object getActorId() {
        return actorID;
    }
}