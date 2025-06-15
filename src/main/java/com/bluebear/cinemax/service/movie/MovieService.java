package com.bluebear.cinemax.service.movie;

import com.bluebear.cinemax.dto.MovieDTO;
import org.springframework.data.domain.Pageable;

import java.util.Date;
import java.util.List;
import java.util.Optional;

public interface MovieService {
    // --- CRUD ---
    MovieDTO createMovie(MovieDTO movieDTO);

    Optional<MovieDTO> getMovieById(Integer id);

    Optional<MovieDTO> updateMovie(Integer id, MovieDTO movieDTO);

    void deleteMovie(Integer id);

    // --- Basic Queries ---
    List<MovieDTO> findAllByStatus(Pageable pageable);

    List<MovieDTO> findAllByStatusOrderByMovieRateDesc(Pageable pageable);

    MovieDTO findMovieByHighestRate();

    List<MovieDTO> findTop3MoviesHighestRate();

    // --- Filter By Name / Genre ---
    List<MovieDTO> findMoviesByGenre(Integer genreId, Pageable pageable);

    List<MovieDTO> findMoviesByName(String movieName, Pageable pageable);

    List<MovieDTO> findMoviesByGenreAndName(Integer genreId, String movieName, Pageable pageable);

    List<MovieDTO> findMoviesByGenreAndNameOrderByRateDesc(Integer genreId, String movieName, Pageable pageable);

    List<MovieDTO> findMoviesByNameOrderByRateDesc(String movieName, Pageable pageable);

    List<MovieDTO> findMoviesByGenreOrderByRateDesc(Integer genreId, Pageable pageable);

    // --- Special Queries ---
    List<MovieDTO> findAllMoviesCurrentlyShow();

    List<MovieDTO> findAllMoviesWillShow();

    List<MovieDTO> findMoviesByScheduleToday(Date today);

    List<MovieDTO> findMoviesByScheduleAndTheater(Date schedule, int theaterId);

    // --- Pagination (Count Pages) ---
    int countNumberOfPage();

    int countNumberOfPage(List<MovieDTO> movies);

    int countNumberOfPageByName(String movieName);

    int countNumberOfPageByGenreId(Integer genreId);

    int countNumberOfPageByGenreAndByName(Integer genreId, String movieName);
}
