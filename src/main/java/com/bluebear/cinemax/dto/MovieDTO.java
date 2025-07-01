package com.bluebear.cinemax.dto;

import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class MovieDTO {

    private Integer movieID;
    private String movieName;
    private String description;
    private String image;
    private String banner;
    private String studio;
    private Integer duration;
    private String trailer;
    private String movieRate;
    private String actor;
    private LocalDate startDate;
    private LocalDate endDate;
    private String status;
}
