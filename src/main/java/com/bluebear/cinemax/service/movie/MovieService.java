package com.bluebear.cinemax.service.movie;

import com.bluebear.cinemax.dto.MovieDTO;
import com.bluebear.cinemax.entity.Movie;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface MovieService {
    MovieDTO createMovie(MovieDTO movieDTO);

    Optional<MovieDTO> getMovieById(Integer id);

    Optional<MovieDTO> updateMovie(Integer id, MovieDTO movieDTO);

    void deleteMovie(Integer id);

    Page<MovieDTO> findAllByStatus(Pageable pageable);

    Page<MovieDTO> findMoviesByGenre(Integer genreId, Pageable pageable);

    Page<MovieDTO> findMoviesByGenreAndName(Integer genreId, String movieName, Pageable pageable);

    Page<MovieDTO> findMoviesByName(String movieName, Pageable pageable);

    Page<MovieDTO> findAllByStatusOrderByMovieRateDesc(Pageable pageable);

    Page<MovieDTO> findAllMoviesCurrentlyShow();

    Page<MovieDTO> findAllMoviesWillShow();

    Page<MovieDTO> findTop3MoviesHighestRate();

    Page<MovieDTO> findTop5MoviesHighestRate();

    MovieDTO findMovieByHighestRate();

    MovieDTO findMovieByIdWithGenresAndActors(Integer id);

    Page<MovieDTO> findMoviesByScheduleToday(LocalDateTime today);

    Page<MovieDTO> findMoviesByScheduleAndTheaterAndRoomType(LocalDateTime schedule, int theaterId, String roomType);

    Page<MovieDTO> findMoviesByScheduleAndTheaterAndRoomTypeAndGenre(LocalDateTime schedule, int theaterId, String roomType, String genreName);

    Page<MovieDTO> findMoviesByScheduleAndTheater(LocalDateTime schedule, String theaterName);

    Page<MovieDTO> findMovies(Integer theaterId, Integer genreId, String movieName, Pageable pageable);

    Page<MovieDTO> findMoviesThatHaveFeedback(Pageable pageable);

    List<MovieDTO> getMoviesByActor(String actorName);

    MovieDTO toDTO(Movie movie);

    Movie toEntity(MovieDTO dto);
}
