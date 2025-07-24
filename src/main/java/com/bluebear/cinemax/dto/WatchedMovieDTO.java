package com.bluebear.cinemax.dto;

import com.bluebear.cinemax.entity.Invoice;
import com.bluebear.cinemax.entity.Movie;
import com.bluebear.cinemax.entity.Schedule;
import com.bluebear.cinemax.entity.Theater;
import lombok.*;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class WatchedMovieDTO {
    private Movie movie;
    private Theater theater;
    private Schedule schedule;
    private Integer invoiceId;
    private LocalDateTime bookingDate;


}
