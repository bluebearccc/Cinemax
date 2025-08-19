package com.bluebear.cinemax.function;

import com.bluebear.cinemax.dto.MovieDTO;
import com.bluebear.cinemax.dto.ScheduleDTO;
import com.bluebear.cinemax.service.movie.MovieService;
import com.bluebear.cinemax.service.schedule.ScheduleService;
import com.bluebear.cinemax.service.theater.TheaterService;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Component
public class ScheduleFunction {
    @Autowired
    private ScheduleService scheduleService;
    @Autowired
    private TheaterService theaterService;
    @Autowired
    private MovieService movieService;

    @Tool(description = "Get schedules at a specific theater in a specific day")
    public List<ScheduleDTO> getSchedulesTodayAtTheater(String theaterName, int dayOffSet) {
        LocalDateTime date = calculateDate( dayOffSet);
        return scheduleService.findSchedulesByTheaterAndDate(theaterService.getTheaterByName(theaterName).getTheaterID(), date.toLocalDate());
    }

    @Tool(description = "Get schedules of a movie at a specific theater in a specific day and in type of room (single or couple)")
    public List<ScheduleDTO> getScheduleByTheaterAndDateAndMovie(String theaterName, int dayOffSet, String movieName, String roomType) {
        LocalDateTime date = calculateDate(dayOffSet);
        return scheduleService.getScheduleByMovieIdAndTheaterIdAndDateAndRoomType(movieService.findMovieByMovieName(movieName).getMovieID(), theaterService.getTheaterByName(theaterName).getTheaterID(), date, roomType).getContent();
    }

    @Tool(description = "Get schedules of a movie at a specific day")
    public List<ScheduleDTO> getScheduleByMovieAndDate(String movieName, int dayOffSet) {
        LocalDateTime date = calculateDate(dayOffSet);
        return scheduleService.getScheduleByMovieIdAndDate(movieService.findMovieByMovieName(movieName).getMovieID(), date).getContent();
    }

    @Tool(description = "Get the number of seat available in a schedule (you need to ask user to get the scheduleId)")
    public ScheduleDTO getAvailableSeatCount(int scheduleId) {
        ScheduleDTO scheduleDTO = scheduleService.getScheduleById(scheduleId);
        scheduleService.calculateNumOfSeatLeft(scheduleDTO);
        return scheduleDTO;
    }

    public LocalDateTime calculateDate(int dayOffSet) {
        if ( dayOffSet == 0 ) {
            return LocalDateTime.now();
        } else {
            return LocalDate.now().plusDays(dayOffSet).atStartOfDay();
        }
    }
}
