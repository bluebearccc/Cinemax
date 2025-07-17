package com.bluebear.cinemax.dto;

import com.bluebear.cinemax.entity.Movie;
import com.bluebear.cinemax.entity.Schedule;
import com.bluebear.cinemax.entity.Theater;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class WatchedMovieDTO {
    private Movie movie;
    private Theater theater;
    private Schedule schedule;
    private Integer invoiceId;

}
