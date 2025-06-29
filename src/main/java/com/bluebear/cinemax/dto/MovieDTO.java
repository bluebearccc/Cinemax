package com.bluebear.cinemax.dto;

import com.bluebear.cinemax.enumtype.Age_Limit;
import com.bluebear.cinemax.enumtype.Movie_Status;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MovieDTO {
    private Integer movieID;
    private String movieName;
    private Age_Limit ageLimit;
    private String description;
    private String image;
    private String banner;
    private String studio;
    private Integer duration;
    private String trailer;
    private Double movieRate;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Movie_Status status;
    private Integer ticketSold;

    private List<GenreDTO> genres;
    private List<ActorDTO> actors;
    private List<MovieFeedbackDTO> movieFeedbacks;
    private List<ScheduleDTO> schedules;

    public String formattedStartDate() {
        return startDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }
    public String formattedEndDate() {
        return endDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }
}