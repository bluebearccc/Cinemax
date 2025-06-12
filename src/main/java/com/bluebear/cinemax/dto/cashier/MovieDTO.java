package com.bluebear.cinemax.dto.cashier;

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
    private String banner;
    private String studio;
    private Integer duration;
    private String trailer;
    private BigDecimal movieRate;
    private String actor;
    private LocalDate startDate;
    private LocalDate endDate;
    private String status;
    private List<Integer> genreIds;
    private List<Integer> scheduleIds;
    private List<Integer> feedbackIds;

    public enum MovieStatus {
        Active, Removed
    }
}