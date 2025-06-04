package com.bluebear.cinemax.dto;

import com.bluebear.cinemax.enums.MovieStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MovieDTO {
    private Integer movieId;
    private String movieName;
    private String description;
    private String image;
    private String studio;
    private String genre;
    private Integer duration;
    private String trailer;
    private BigDecimal movieRate;
    private String actor;
    private LocalDate startDate;
    private LocalDate endDate;
    private MovieStatus status;
    private List<GenreDTO> genres;
}