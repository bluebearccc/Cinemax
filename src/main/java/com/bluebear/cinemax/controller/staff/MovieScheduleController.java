package com.bluebear.cinemax.controller.staff;

import com.bluebear.cinemax.entity.Movie;
import com.bluebear.cinemax.service.staff.MovieService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequestMapping("/movie_schedule")
public class MovieScheduleController {

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
}
