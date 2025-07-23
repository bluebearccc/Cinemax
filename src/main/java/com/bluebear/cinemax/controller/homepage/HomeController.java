package com.bluebear.cinemax.controller.homepage;

import com.bluebear.cinemax.constant.Constant;
import com.bluebear.cinemax.dto.GenreDTO;
import com.bluebear.cinemax.dto.MovieDTO;
import com.bluebear.cinemax.dto.MovieFeedbackDTO;
import com.bluebear.cinemax.dto.TheaterDTO;
import com.bluebear.cinemax.service.genre.GenreService;
import com.bluebear.cinemax.service.movie.MovieService;
import com.bluebear.cinemax.service.moviefeedback.MovieFeedbackService;
import com.bluebear.cinemax.service.theater.TheaterService;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;
import java.util.*;

@Controller
@RequestMapping()
public class HomeController {

    @Autowired
    private MovieService movieService;
    @Autowired
    private GenreService genreService;
    @Autowired
    private MovieFeedbackService movieFeedbackService;
    @Autowired
    private TheaterService theaterService;

    private Page<MovieDTO> topMovies;
    private Page<MovieDTO> currentMovies;
    private Page<MovieDTO> futureMovies;
    private List<GenreDTO> genres;
    private Page<TheaterDTO> theaters;
    private Page<MovieDTO> theaterMovies;
    private Page<MovieDTO> moviesHaveFeedback;
    private TheaterDTO currentTheater;
    private Map<MovieDTO, Page<MovieFeedbackDTO>> movieFeedbacks;

    int maximumFeedback;
    int currentNumberOfFeedback;
    String currentWebPage;

    @PostConstruct public void init() {
        genres = genreService.getAllGenres();
        theaters = theaterService.getAllTheaters();
        maximumFeedback = movieFeedbackService.countDistinctMovieByFeedBack();
    }

    @GetMapping()
    public String home(Model model) {
        movieFeedbacks = new LinkedHashMap<>();
        currentMovies = movieService.findAllMoviesCurrentlyShow();
        moviesHaveFeedback = movieService.findMoviesThatHaveFeedback(Pageable.unpaged());
        for (MovieDTO movieDTO : moviesHaveFeedback.getContent().subList(0, Constant.FEEDBACK_PER_ROW)) {
            movieFeedbacks.put(movieDTO, movieFeedbackService.getAllByMovieId(movieDTO.getMovieID(), PageRequest.of(0,Constant.FEEDBACK_PER_CELL)));
        }

        String roomType = "single";
        boolean isNearest = false;

        currentWebPage = "home";
        currentNumberOfFeedback = movieFeedbacks.size();
        currentTheater = theaters.getContent().getFirst();
        futureMovies = movieService.findAllMoviesWillShow();
        topMovies = movieService.findTop3MoviesHighestRate();
        theaterMovies = movieService.findMoviesByScheduleAndTheaterAndRoomType(LocalDateTime.now(), currentTheater.getTheaterID(), roomType);

        model.addAttribute("isNearest", isNearest);
        model.addAttribute("roomType", roomType);
        model.addAttribute("currentWebPage", currentWebPage);
        model.addAttribute("currentTheater", currentTheater);
        model.addAttribute("theaterMovies", theaterMovies);
        model.addAttribute("maximumFeedback", maximumFeedback);
        model.addAttribute("currentNumberOfFeedback", currentNumberOfFeedback);
        model.addAttribute("feedbacks", movieFeedbacks);
        model.addAttribute("currentMovies", currentMovies);
        model.addAttribute("futureMovies", futureMovies);
        model.addAttribute("topMovies", topMovies);
        model.addAttribute("theaters", theaters);
        model.addAttribute("genres", genres);
        return "customer/home";
    }

    @GetMapping("/home/loadFeedback")
    public String loadFeedback(Model model, @RequestParam(name = "currentNumberOfFeedback") int currentNumOfFeedback) {
        int endIndex = currentNumOfFeedback + Constant.FEEDBACK_PER_ROW;
        if (endIndex > maximumFeedback) {
            endIndex = currentNumOfFeedback + (maximumFeedback - currentNumOfFeedback);
        }

        for (MovieDTO movieDTO : moviesHaveFeedback.getContent().subList(0, endIndex)) {
            movieFeedbacks.put(movieDTO, movieFeedbackService.getAllByMovieId(movieDTO.getMovieID(), PageRequest.of(0,Constant.FEEDBACK_PER_CELL)));
        }

        currentNumberOfFeedback = endIndex;

        model.addAttribute("maximumFeedback", maximumFeedback);
        model.addAttribute("feedbacks", movieFeedbacks);
        model.addAttribute("currentNumberOfFeedback", currentNumberOfFeedback);
        return "customer/fragments/homepage/feedback-area :: feedback-area";
    }

    @GetMapping("/home/loadBookMovie")
    public String loadBookMovie(Model model, @RequestParam(name = "selectedIndex") int selectedIndex, @RequestParam(name = "theaterId") int theaterId, @RequestParam(name = "roomType") String roomType, @RequestParam(name = "isNearest") Boolean isNearest) {
        LocalDateTime dateTime = LocalDateTime.now();
        if (selectedIndex > 0) {
            dateTime = LocalDateTime.now().plusDays(selectedIndex).toLocalDate().atStartOfDay();
        }
        theaterMovies = movieService.findMoviesByScheduleAndTheaterAndRoomType(dateTime, theaterId, roomType);
        currentTheater = theaterService.getTheaterById(theaterId);
        model.addAttribute("isNearest", isNearest);
        model.addAttribute("roomType", roomType);
        model.addAttribute("theaterMovies", theaterMovies);
        model.addAttribute("currentTheater", currentTheater);
        model.addAttribute("theaters", theaters);
        model.addAttribute("selectedIndex", selectedIndex);
        return "customer/fragments/homepage/book-area :: book-movie-area";
    }

}
