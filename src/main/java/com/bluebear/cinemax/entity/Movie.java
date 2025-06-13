package com.bluebear.cinemax.entity;
import jakarta.persistence.*;
import lombok.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.Objects;

@Setter
@Getter
@Entity
@Table(name = "Movie")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Movie {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer movieID;

    @Column(nullable = false, length = 255)
    private String movieName;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String image;

    private String banner;

    private String studio;

    private Integer duration; // in minutes

    private String trailer;

    private String movieRate;

    private String actor;

    private LocalDate startDate;

    private LocalDate endDate;

    @Column(nullable = false)
    private String status;
}