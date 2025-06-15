package com.bluebear.cinemax.dto;

import com.bluebear.cinemax.enumtype.Movie_Status;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MovieDTO {
    private Integer movieID;
    private String movieName;
    private String description;
    private String image;
    private String banner;
    private String studio;
    private int duration;
    private String trailer;
    private Double movieRate;
    private String actor;
    private Date startDate;
    private Date endDate;
    private Movie_Status status;

    private List<GenreDTO> genres;
    private List<MovieFeedbackDTO> movieFeedbacks;
    private List<ScheduleDTO> schedules;
}