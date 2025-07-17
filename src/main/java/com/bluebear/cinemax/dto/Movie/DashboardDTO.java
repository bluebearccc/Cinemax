package com.bluebear.cinemax.dto.Movie;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DashboardDTO {
    private int showingMovies;
    private double revenueToday;
    private double revenueYear;
    private double revenueMonth;
    private Long ticketsToday;
    private Integer ticketsMonth;
    private Integer ticketsThisYear;
}
