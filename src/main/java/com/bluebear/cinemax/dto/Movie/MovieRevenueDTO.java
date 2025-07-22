package com.bluebear.cinemax.dto.Movie;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MovieRevenueDTO {
    private Integer movieId;
    private String movieName;
    private String image;
    private Long totalTickets;
    private Double totalRevenue;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
}
