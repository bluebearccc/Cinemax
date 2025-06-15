package com.bluebear.cinemax.controller;

import com.bluebear.cinemax.constant.Constant;
import com.bluebear.cinemax.dto.GenreDTO;
import com.bluebear.cinemax.dto.MovieDTO;
import com.bluebear.cinemax.dto.MovieFeedbackDTO;
import com.bluebear.cinemax.dto.TheaterDTO;
import com.bluebear.cinemax.service.genre.GenreService;
import com.bluebear.cinemax.service.moviefeedback.MovieFeedbackService;
import com.bluebear.cinemax.service.movie.MovieService;
import com.bluebear.cinemax.service.theater.TheaterService;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.*;

@Controller
//@RestController
public class HomeController {

    @Autowired
    private MovieService movieService;
    @Autowired
    private GenreService genreService;
    @Autowired
    private MovieFeedbackService movieFeedbackService;
    @Autowired
    private TheaterService theaterService;

    private List<MovieDTO> topMovies;
    private List<MovieDTO> currentMovies;
    private List<MovieDTO> futureMovies;
    private List<GenreDTO> genres;
    private List<TheaterDTO> theaters;
    private List<MovieDTO> theaterMovies;
    private TheaterDTO currentTheater;
    private Map<MovieDTO, List<MovieFeedbackDTO>> movieFeedbacks = new LinkedHashMap<>();

    int maximumFeedback;
    int currentNumberOfFeedback;

    @PostConstruct public void init() {
        maximumFeedback = movieFeedbackService.countDistinctMovieByFeedBack();
    }

    @GetMapping("/home")
    public String home(Model model) {
        currentMovies = movieService.findAllMoviesCurrentlyShow();
        for (MovieDTO movieDTO : currentMovies.subList(0, Constant.FEEDBACK_PER_ROW)) {
            movieFeedbacks.put(movieDTO, movieFeedbackService.getAllByMovieId(movieDTO.getMovieID(), PageRequest.of(0,Constant.FEEDBACK_PER_CELL)));
        }

        currentNumberOfFeedback = movieFeedbacks.size();
        theaters = theaterService.getAllTheaters();
        currentTheater = theaters.getFirst();
        futureMovies = movieService.findAllMoviesWillShow();
        topMovies = movieService.findTop3MoviesHighestRate();
        theaterMovies = movieService.findMoviesByScheduleAndTheater(new Date(), 1);

        model.addAttribute("currentTheater", currentTheater);
        model.addAttribute("theaterMovies", theaterMovies);
        model.addAttribute("maximumFeedback", maximumFeedback);
        model.addAttribute("feedbackPage", 0);
        model.addAttribute("currentNumberOfFeedback", currentNumberOfFeedback);
        model.addAttribute("feedbacks", movieFeedbacks);
        model.addAttribute("currentMovies", currentMovies);
        model.addAttribute("futureMovies", futureMovies);
        model.addAttribute("topMovies", topMovies);
        model.addAttribute("theaters", theaters);
        return "/customer/home";
    }

    @GetMapping("/loadFeedback")
    public String loadFeedback(Model model, @RequestParam(name = "currentNumberOfFeedback") int currentNumOfFeedback) {
        int endIndex = currentNumOfFeedback + Constant.FEEDBACK_PER_ROW;
        if (endIndex > maximumFeedback) {
            endIndex = currentNumOfFeedback + (maximumFeedback - currentNumOfFeedback);
        }

        for (MovieDTO movieDTO : currentMovies.subList(0, endIndex)) {
            movieFeedbacks.put(movieDTO, movieFeedbackService.getAllByMovieId(movieDTO.getMovieID(), PageRequest.of(0,Constant.FEEDBACK_PER_CELL)));
        }

        currentNumberOfFeedback = endIndex;

        model.addAttribute("maximumFeedback", maximumFeedback);
        model.addAttribute("feedbacks", movieFeedbacks);
        model.addAttribute("currentNumberOfFeedback", currentNumberOfFeedback);
        return "customer/fragments/homepage/feedback-area :: feedback-area";
    }

    @GetMapping("/loadBookMovie")
    public String loadBookMovie(Model model, @RequestParam(name = "selectedIndex") int selectedIndex, @RequestParam(name = "theaterId") int theaterId) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, + selectedIndex);
        theaterMovies = movieService.findMoviesByScheduleAndTheater(calendar.getTime(), theaterId);
        System.out.println("hehehe");
        currentTheater = theaterService.getTheaterById(theaterId);
        model.addAttribute("theaterMovies", theaterMovies);
        model.addAttribute("currentTheater", currentTheater);
        model.addAttribute("theaters", theaters);
        model.addAttribute("selectedIndex", selectedIndex);
        return "customer/fragments/homepage/book-area :: book-movie-area";
    }

}
