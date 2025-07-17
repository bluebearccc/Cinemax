package com.bluebear.cinemax.entity;

import com.bluebear.cinemax.enumtype.AgeLimit;
import com.bluebear.cinemax.enumtype.Movie_Status;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "Movie")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Movie {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MovieID")
    private Integer movieID;

    @Column(name = "MovieName", nullable = false, length = 255)
    private String movieName;

    @Column(name = "Age_limit", length = 10)
    @Enumerated(EnumType.STRING)
    private AgeLimit ageLimit;

    @Column(name = "Description", length = 1000)
    private String description;

    @Column(name = "Image", nullable = false, length = 255)
    private String image;

    @Column(name = "Banner", nullable = false, length = 255)
    private String banner;

    @Column(name = "Studio", length = 100)
    private String studio;

    @Column(name = "Duration", nullable = false)
    private Integer duration;

    @Column(name = "Trailer", nullable = false, length = 255)
    private String trailer;

    @Column(name = "MovieRate", nullable = true)
    private Double movieRate;

    @Column(name = "StartDate", nullable = false)
    private LocalDateTime startDate;

    @Column(name = "EndDate", nullable = false)
    private LocalDateTime endDate;

    @Column(name = "Status", nullable = false, length = 10)
    @Enumerated(EnumType.STRING)
    private Movie_Status status;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "Movie_Genre",
            joinColumns = @JoinColumn(name = "MovieID"),
            inverseJoinColumns = @JoinColumn(name = "GenreID")
    )
    private List<Genre> genres;

    @ManyToMany
    @JoinTable(
            name = "Movie_Actor",
            joinColumns = @JoinColumn(name = "MovieID"),
            inverseJoinColumns = @JoinColumn(name = "ActorID")
    )
    private List<Actor> actors;

    @OneToMany(mappedBy = "movie", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<MovieFeedback> feedbackList;

    @OneToMany(mappedBy = "movie", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Schedule> scheduleList;
}
