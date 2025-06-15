package com.bluebear.cinemax.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping
public class MovieController {

//    @Autowired
//    private MovieService movieService;
//
//    @Autowired
//    private GenreService genreService;
//
//    @Autowired
//    private MovieGenreService movieGenreService;

    @GetMapping("/movies")
    public String toString(Model model) {
//        List<MovieDTO> movies = movieService.getAllMovies();
//        List<GenreDTO> genres = genreService.getAllGenres();
//        model.addAttribute("genres", genres);
//        model.addAttribute("movies", movies);
        return "/customer/movie-list";
    }

//    @GetMapping("/movielist")
//    public List<MovieDTO> getMovieList() {
//        return movieService.getAllMovies();
//    }
//
//    @GetMapping("/moviegenre")
//    public List<GenreDTO> getGenreList() {
//        return genreService.getAllGenres();
//    }
//
//    @GetMapping("/moviegenrejoin")
//    public List<MovieGenreDTO> getMovieGenreList() {
//        return movieGenreService.getAllMovieGenres();
//    }
}
