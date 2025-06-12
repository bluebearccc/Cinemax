package com.bluebear.cinemax.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Movie_Genre {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "MovieID", nullable = false, referencedColumnName = "MovieID")
    private Movie movie;

    @ManyToOne
    @JoinColumn(name = "GenreID", nullable = false, referencedColumnName = "GenreID")
    private Genre genre;

}
