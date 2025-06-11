package com.bluebear.cinemax.entity;

import jakarta.annotation.Generated;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "Genre")
public class Genre {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "GenreID")
    private Integer genreID;
    @Column(name = "GenreName", length = 255, nullable = false)
    private String genreName;
    @OneToMany(mappedBy = "genre")
    private Set<Movie_Genre> movie_Genre;

}
