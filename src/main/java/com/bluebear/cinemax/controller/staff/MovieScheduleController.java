package com.bluebear.cinemax.controller.staff;

import com.bluebear.cinemax.entity.Movie;
import com.bluebear.cinemax.entity.Schedule;
import com.bluebear.cinemax.service.staff.MovieService;
import com.bluebear.cinemax.service.staff.ScheduleService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/movie_schedule")
public class MovieScheduleController {

    @Autowired
    ScheduleService scheduleService;

    @Autowired
    MovieService movieService;

    @GetMapping
    public String getMovieSchedulePage(Model model) {
        List<Movie> movies = movieService.findAllShowingMovies();
        model.addAttribute("movies", movies);
        return "staff/movie_schedule";
    }

    @GetMapping("/search")
    public String searchMovieByName(@RequestParam("name") String name, Model model) {
        List<Movie> movies = movieService.searchMovieByName(name);
        model.addAttribute("movies", movies);
        return "staff/movie_schedule";
    }

    @GetMapping("/show_schedule")
    public String showSchedule(@RequestParam("movieId") Integer movieID, Model model) {
        Movie movie = movieService.getMovieById(movieID);
        List<Schedule> schedules = scheduleService.findByMovieId(movieID);
        model.addAttribute("movie", movie);
        model.addAttribute("schedules", convertSchedulesToJsonString(schedules));
        return "staff/movie_schedule_detail";
    }

    private String convertSchedulesToJsonString(List<Schedule> schedules) {
        Map<String, List<Map<String, String>>> schedulesGroupedByDate = new java.util.HashMap<>();
        for (Schedule schedule : schedules) {
            String dateKey = schedule.getShowDateKey();
            List<Map<String, String>> schedulesForDay;
            if (schedulesGroupedByDate.containsKey(dateKey)) {
                schedulesForDay = schedulesGroupedByDate.get(dateKey);
            } else {
                 schedulesForDay = new java.util.ArrayList<>();
                 schedulesGroupedByDate.put(dateKey, schedulesForDay);
            }
            Map<String, String> scheduleInfo = new java.util.HashMap<>();
            scheduleInfo.put("time", schedule.getFormattedStartTime());
            scheduleInfo.put("endTime", schedule.getFormattedEndTime());
            scheduleInfo.put("room", schedule.getRoom().getName());
            schedulesForDay.add(scheduleInfo);
        }

        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.writeValueAsString(schedulesGroupedByDate);
        } catch (JsonProcessingException e) {
            System.out.println(e.getMessage());
            return "{}";
        }
    }
}
