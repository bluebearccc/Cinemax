package com.bluebear.cinemax.controller;

import com.bluebear.cinemax.constant.Constant;
import com.bluebear.cinemax.dto.GenreDTO;
import com.bluebear.cinemax.dto.MovieDTO;
import com.bluebear.cinemax.dto.TheaterDTO;
import com.bluebear.cinemax.service.genre.GenreService;
import com.bluebear.cinemax.service.movie.MovieService;
import com.bluebear.cinemax.service.theater.TheaterService;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequestMapping
public class MovieController {

    @Autowired
    private MovieService movieService;

    @Autowired
    private GenreService genreService;

    @Autowired
    private TheaterService theaterService;

    private MovieDTO topMovie;

    List<MovieDTO> movies;
    List<GenreDTO> genres;
    List<TheaterDTO> theaters;

    @PostConstruct
    public void preInit() {
        topMovie = movieService.findMovieByHighestRate();
        theaters = theaterService.getAllTheaters();
    }

    @GetMapping("/movies")
    public String toString(Model model, @RequestParam(name = "movieGenre", required = false) Integer genreId, @RequestParam(name = "movieName", required = false) String movieName, @RequestParam(name = "page", required = false) Integer page, @RequestParam(name = "sortBy", required = false) String sort, @RequestParam(name = "isFirst", required = false) Boolean isFirst) {

        int totalPage = 0;
        int currentPage = page == null ? 1 : page;
        Pageable pageable = PageRequest.of(currentPage - 1, Constant.MOVIES_PER_PAGE);
        genres = genreService.getAllGenres();
        if (genreId != null && movieName != null) {
            if ("high-rate".equals(sort)) {
                movies = movieService.findMoviesByGenreAndNameOrderByRateDesc(genreId, movieName.trim(), pageable);
            } else {
                movies = movieService.findMoviesByGenreAndName(genreId, movieName.trim(), pageable);
            }
            totalPage = movieService.countNumberOfPageByGenreAndByName(genreId, movieName.trim());
        } else if (genreId != null) {
            if ("high-rate".equals(sort)) {
                movies = movieService.findMoviesByGenreOrderByRateDesc(genreId, pageable);
            } else {
                movies = movieService.findMoviesByGenre(genreId, pageable);
            }
            totalPage = movieService.countNumberOfPageByGenreId(genreId);
        } else if (movieName != null) {
            if ("high-rate".equals(sort)) {
                movies = movieService.findMoviesByNameOrderByRateDesc(movieName.trim(), pageable);
            } else {
                movies = movieService.findMoviesByName(movieName.trim(), pageable);
            }
            totalPage = movieService.countNumberOfPageByName(movieName.trim());
        } else {
            if ("high-rate".equals(sort)) {
                movies = movieService.findAllByStatusOrderByMovieRateDesc(pageable);
            } else {
                movies = movieService.findAllByStatus(pageable);
            }
            totalPage = movieService.countNumberOfPage();
        }

        System.out.println("sort: " + sort);

        model.addAttribute("genreId", genreId);
        model.addAttribute("movieName", movieName);
        model.addAttribute("sortBy", sort);
        model.addAttribute("currentPage", currentPage);
        model.addAttribute("TotalPage", totalPage);
        model.addAttribute("genres", genres);
        model.addAttribute("movies", movies);
        model.addAttribute("theaters", theaters);
        model.addAttribute(Constant.TOP_MOVIE, topMovie);

        if (isFirst == null) {
            return "/customer/movie-list";
        } else {
            return "/customer/fragments/movie-list/list-movie :: list-movie";
        }

    }

}
