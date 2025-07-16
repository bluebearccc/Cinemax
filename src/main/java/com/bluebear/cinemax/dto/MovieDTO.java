package com.bluebear.cinemax.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MovieDTO {
    private Integer movieId;
    private String movieName;
    private String description;
    private String image;
    private String banner;
    private String studio;
    private Integer duration;
    private String trailer;
    private double movieRate;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String status;
    private List<String> genres;
    private List<String> actors;


}