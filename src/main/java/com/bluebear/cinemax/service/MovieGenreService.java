package com.bluebear.cinemax.service;

import com.bluebear.cinemax.entity.MovieGenre;
import com.bluebear.cinemax.repository.MovieGenreRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

@Service
public class MovieGenreService {

    private static final Logger logger = LoggerFactory.getLogger(MovieGenreService.class);

    @Autowired
    private MovieGenreRepository movieGenreRepository;

    public List<MovieGenre> getAllMovieGenres() {
        List<MovieGenre> genres = movieGenreRepository.findAll();
        logger.info("Retrieved {} movie genres from database", genres.size());
        return genres;
    }

    public Optional<MovieGenre> getMovieGenreById(int id) {
        return movieGenreRepository.findById(id);
    }

    public MovieGenre saveMovieGenre(MovieGenre movieGenre) {
        return movieGenreRepository.save(movieGenre);
    }

    public void deleteMovieGenre(int id) {
        movieGenreRepository.deleteById(id);
    }

    public List<MovieGenre> findByGenreId(int genreId) {
        return movieGenreRepository.findByGenreId(genreId);
    }

    public List<MovieGenre> findByMovieId(int movieId) {
        return movieGenreRepository.findByMovieId(movieId);
    }
}
