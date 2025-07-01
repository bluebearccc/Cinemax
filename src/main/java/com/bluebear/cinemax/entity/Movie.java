package com.bluebear.cinemax.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

@Entity
@Table(name = "Movie")
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"movieGenres", "movieActors"}) // Avoid circular reference in toString
public class Movie {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MovieID")
    private Integer movieId;

    @Column(name = "MovieName", nullable = false)
    private String movieName;

    @Column(name = "Description", columnDefinition = "nvarchar(MAX)")
    private String description;

    @Column(name = "Image", nullable = false)
    private String image;

    @Column(name = "Banner", nullable = false)
    private String banner;

    @Column(name = "Studio")
    private String studio;

    @Column(name = "Duration", nullable = false)
    private Integer duration;

    @Column(name = "Trailer", nullable = false)
    private String trailer;

    @Column(name = "MovieRate", precision = 3, scale = 1)
    private BigDecimal movieRate;

    @Column(name = "StartDate", nullable = false)
    private LocalDate startDate;

    @Column(name = "EndDate", nullable = false)
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "Status", nullable = false)
    private MovieStatus status;

    @OneToMany(mappedBy = "movie", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<MovieGenre> movieGenres;

    @OneToMany(mappedBy = "movie", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<MovieActor> movieActors;

    // Enum for Status
    public enum MovieStatus {
        Active, Removed
    }

    // Constructor without relationships for basic creation
    public Movie(String movieName, String image, String banner, Integer duration,
                 String trailer, LocalDate startDate, LocalDate endDate, MovieStatus status) {
        this.movieName = movieName;
        this.image = image;
        this.banner = banner;
        this.duration = duration;
        this.trailer = trailer;
        this.startDate = startDate;
        this.endDate = endDate;
        this.status = status;
    }
}