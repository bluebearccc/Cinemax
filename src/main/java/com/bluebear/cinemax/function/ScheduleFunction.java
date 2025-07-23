package com.bluebear.cinemax.function;

import com.bluebear.cinemax.dto.MovieDTO;
import com.bluebear.cinemax.service.movie.MovieService;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ScheduleFunction {
    @Autowired
    private MovieService movieService;

    @Tool(description = "Get good movies")
    public MovieDTO getMovieRating() {
        return movieService.findMovieByHighestRate();
    }
}
