package com.bluebear.cinemax.dto;

import com.bluebear.cinemax.enumtype.AgeLimit;
import com.bluebear.cinemax.enumtype.Movie_Status;
import lombok.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MovieDTO {
    private Integer movieID;
    private String movieName;
    private AgeLimit ageLimit;
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