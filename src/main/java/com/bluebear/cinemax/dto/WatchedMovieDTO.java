package com.bluebear.cinemax.dto;

import com.bluebear.cinemax.entity.Movie;
import com.bluebear.cinemax.entity.Schedule;
import com.bluebear.cinemax.entity.Theater;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WatchedMovieDTO {
    private Movie movie;
    private Theater theater;
    private Schedule schedule;


    public WatchedMovieDTO(Movie movie, Theater theater) {
        this.movie = movie;
        this.theater = theater;
    }

}
