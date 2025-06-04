package com.bluebear.cinemax.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "Movie_Genre")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MovieGenre {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "GenreID", nullable = false)
    private Genre genre;

    @ManyToOne
    @JoinColumn(name = "MovieID", nullable = false)
    private Movie movie;
}