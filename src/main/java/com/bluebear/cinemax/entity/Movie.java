package com.bluebear.cinemax.entity;

import com.bluebear.cinemax.enums.MovieStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "Movie")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Movie {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MovieID")
    private Integer movieId;

    @Column(name = "MovieName", nullable = false)
    private String movieName;

    @Column(name = "Description", length = 1000)
    private String description;

    @Column(name = "Image", nullable = false)
    private String image;

    @Column(name = "Studio", length = 100)
    private String studio;

    @Column(name = "Genre", nullable = false)
    private String genre;

    @Column(name = "Duration", nullable = false)
    private Integer duration;

    @Column(name = "Trailer", nullable = false)
    private String trailer;

    @Column(name = "MovieRate", precision = 3, scale = 1)
    private BigDecimal movieRate;

    @Column(name = "Actor", nullable = false, columnDefinition = "NVARCHAR(MAX)")
    private String actor;

    @Column(name = "StartDate", nullable = false)
    private LocalDate startDate;

    @Column(name = "EndDate", nullable = false)
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "Status", length = 10)
    private MovieStatus status;

    @OneToMany(mappedBy = "movie", cascade = CascadeType.ALL)
    private List<MovieGenre> movieGenres;

    @OneToMany(mappedBy = "movie", cascade = CascadeType.ALL)
    private List<Schedule> schedules;

    @OneToMany(mappedBy = "movie", cascade = CascadeType.ALL)
    private List<MovieFeedback> movieFeedbacks;
}