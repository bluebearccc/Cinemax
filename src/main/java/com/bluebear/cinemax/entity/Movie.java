package com.bluebear.cinemax.entity;

import com.bluebear.cinemax.enums.Movie_Status;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Nationalized;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Set;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "Movie")
public class Movie {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MovieID")
    private Integer movieId;

    @Nationalized
    @Column(name = "MovieName", length = 255, nullable = false)
    private String movieName;

    @Nationalized
    @Column(name = "Description", length = 1000)
    private String description;

    @Column(name = "Image", length = 255, nullable = false)
    private String image;

    @Column(name = "Banner", length = 255, nullable = false)
    private String banner;

    @Nationalized
    @Column(name = "Studio", length = 100)
    private String studio;

    @Column(name = "Duration", nullable = false)
    private Integer duration;

    @Column(name = "Trailer", length = 255, nullable = false)
    private String trailer;

    @Column(name = "MovieRate", precision = 3, scale = 1, nullable = false)
    private BigDecimal movieRate;

    @Nationalized
    @Column(name = "Actor", columnDefinition = "nvarchar(max)", nullable = false)
    private String actor;

    @Column(name = "StartDate", nullable = false)
    private LocalDateTime startDate;

    @Column(name = "EndDate", nullable = false)
    private LocalDateTime endDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "Status", length = 10, nullable = false)
    private Movie_Status status;

    @OneToMany(mappedBy = "movie")
    private Set<Movie_Genre> movie_Genre;

    @OneToMany(mappedBy = "movie")
    @JsonManagedReference
    private Set<Schedule> schedule;

    public String formattedStartDate() {
        return startDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }
    public String formattedEndDate() {
        return endDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }
}
