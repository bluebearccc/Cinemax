package com.bluebear.cinemax.controller.homepage;

import com.bluebear.cinemax.constant.Constant;
import com.bluebear.cinemax.dto.GenreDTO;
import com.bluebear.cinemax.dto.MovieDTO;
import com.bluebear.cinemax.dto.TheaterDTO;
import com.bluebear.cinemax.service.genre.GenreService;
import com.bluebear.cinemax.service.movie.MovieService;
import com.bluebear.cinemax.service.theater.TheaterService;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequestMapping("/customer/movies")
public class MovieController {

    @Autowired
    private MovieService movieService;

    @Autowired
    private GenreService genreService;

    @Autowired
    private TheaterService theaterService;

    private MovieDTO topMovie;
    private TheaterDTO currentTheater;

    Page<MovieDTO> movies;
    List<GenreDTO> genres;
    Page<TheaterDTO> theaters;
    String currentWebPage;

    @PostConstruct
    public void preInit() {
        topMovie = movieService.findMovieByHighestRate();
        theaters = theaterService.getAllTheaters();
        genres = genreService.getAllGenres();
    }

    @GetMapping
    public String Movies(Model model, @RequestParam(name = "movieGenre", required = false) Integer genreId, @RequestParam(name = "movieName", required = false) String movieName, @RequestParam(name = "page", required = false) Integer page, @RequestParam(name = "sortBy", required = false) String sort, @RequestParam(name = "isFirst", required = false) Boolean isFirst, @RequestParam(name = "selectedTheater", required = false) Integer theaterId) {

        int currentPage = page == null ? 1 : page;
        Pageable pageable = PageRequest.of(currentPage - 1, Constant.MOVIES_PER_PAGE);
        movieName = movieName == null ? "" : movieName.trim();

        if ("rating".equals(sort)) {
            pageable = PageRequest.of(currentPage - 1, Constant.MOVIES_PER_PAGE, Sort.by("movieRate").descending());
            movies = movieService.findMovies(theaterId, genreId, movieName, pageable);
        } else {
            movies = movieService.findMovies(theaterId, genreId, movieName, pageable);
        }

        currentWebPage = "movies";
        model.addAttribute("currentWebPage", currentWebPage);
        model.addAttribute("genreId", genreId);
        model.addAttribute("movieName", movieName);
        model.addAttribute("sortBy", sort);
        model.addAttribute("currentPage", currentPage);
        model.addAttribute("genres", genres);
        model.addAttribute("movies", movies);
        model.addAttribute("theaters", theaters);
        model.addAttribute(Constant.TOP_MOVIE, topMovie);

        if (isFirst == null) {
            return "customer/movie-list";
        } else {
            return "customer/fragments/movie-list/list-movie :: list-movie";
        }

    }

}
