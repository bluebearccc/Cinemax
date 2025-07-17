package com.bluebear.cinemax.controller;

import com.bluebear.cinemax.dto.GenreDTO;
import com.bluebear.cinemax.dto.MovieDTO;
import com.bluebear.cinemax.dto.TheaterDTO;
import com.bluebear.cinemax.service.genre.GenreService;
import com.bluebear.cinemax.service.movie.MovieService;
import com.bluebear.cinemax.service.theater.TheaterService;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/customer/theater-detail")
public class TheaterController {

    @Autowired
    private TheaterService theaterService;
    @Autowired
    private MovieService movieService;
    @Autowired
    private GenreService genreService;

    List<GenreDTO> genres;
    Page<TheaterDTO> theaters;
    Page<MovieDTO> theaterMovies;
    Page<MovieDTO> top5movies;
    String currentWebPage;


    @PostConstruct
    public void preInit() {
        top5movies = movieService.findTop5MoviesHighestRate();
        theaters = theaterService.getAllTheaters();
        genres = genreService.getAllGenres();
    }

    @GetMapping()
    public String theaterDetail(Model model, @RequestParam(name = "theaterId") int theaterId) {
        String roomType = "single";
        currentWebPage = "theaters";
        TheaterDTO theaterDTO = theaterService.getTheaterByIdWithRateCounts(theaterId);
        theaterMovies = movieService.findMoviesByScheduleAndTheaterAndRoomType(LocalDateTime.now(), theaterId, roomType);

        model.addAttribute("genres", genres);
        model.addAttribute("theaterMovies", theaterMovies);
        model.addAttribute("roomType", roomType);
        model.addAttribute("top5movies", top5movies);
        model.addAttribute("currentTheater", theaterDTO);
        model.addAttribute("theaters", theaters);
        model.addAttribute("currentWebPage", currentWebPage);
        return "customer/theater-detail";
    }

    @GetMapping("/loadSchedule")
    public String loadSchedule(Model model, @RequestParam(name = "theaterId") int theaterId, @RequestParam(name = "selectedIndex") int selectedIndex, @RequestParam(name = "roomType") String roomType) {
        LocalDateTime dateTime = LocalDateTime.now();
        if (selectedIndex > 0) {
            dateTime = LocalDateTime.now().plusDays(selectedIndex).toLocalDate().atStartOfDay();
        }
        theaterMovies = movieService.findMoviesByScheduleAndTheaterAndRoomType(dateTime, theaterId, roomType);
        model.addAttribute("roomType", roomType);
        model.addAttribute("theaterMovies", theaterMovies);
        model.addAttribute("selectedIndex", selectedIndex);
        return "customer/fragments/theater-detail/book-theater-detail :: book-theater-detail";
    }

}
