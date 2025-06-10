package com.bluebear.cinemax.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.persistence.*;
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

    @Column(name = "Banner", nullable = false)
    private String banner;

    @Column(name = "Studio", length = 100)
    private String studio;

    @Column(name = "Duration", nullable = false)
    private Integer duration;

    @Column(name = "Trailer", nullable = false)
    private String trailer;

    @Column(name = "MovieRate", nullable = false, precision = 3, scale = 1)
    private BigDecimal movieRate;

    @Column(name = "Actor", nullable = false, columnDefinition = "nvarchar(MAX)")
    private String actor;

    @Column(name = "StartDate", nullable = false)
    private LocalDate startDate;

    @Column(name = "EndDate", nullable = false)
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "Status", nullable = false)
    private MovieStatus status;

    @OneToMany(mappedBy = "movie", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<MovieGenre> movieGenres;

    @OneToMany(mappedBy = "movie", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Schedule> schedules;

    @OneToMany(mappedBy = "movie", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<MovieFeedback> movieFeedbacks;

    public enum MovieStatus {
        Active, Removed
    }
}