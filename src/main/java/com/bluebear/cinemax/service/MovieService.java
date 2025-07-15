package com.bluebear.cinemax.service;

import com.bluebear.cinemax.entity.Movie;
import com.bluebear.cinemax.repository.MovieRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.sql.ClientInfoStatus;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
@Service
public class MovieService {
    private static final Logger logger = LoggerFactory.getLogger(MovieService.class);
    @Autowired
    private MovieRepository movieRepository;

    public List<Movie> getAllMovies() {
        List<Movie> movies = movieRepository.findAll();
        return movies;
    }

    public Optional<Movie> getMovieById(Integer id) {
        return movieRepository.findById(id);
    }

    public Movie saveMovie(Movie movie) {
        return movieRepository.save(movie);
    }

    public void deleteMovie(Integer id) {
        movieRepository.deleteById(id);
    }

    public List<Movie> searchMoviesByName(String keyword) {
        List<Movie> movies = movieRepository.findByMovieNameContaining(keyword);
        return movies;
    }
}
