package com.bluebear.cinemax.service.movie;

import com.bluebear.cinemax.dto.MovieDTO;
import com.bluebear.cinemax.entity.Movie;
import com.bluebear.cinemax.enumtype.AgeLimit;
import com.bluebear.cinemax.enumtype.Movie_Status;
import com.bluebear.cinemax.enumtype.Theater_Status;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
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


    Page<MovieDTO> findMovies(Integer theaterId, Integer genreId, String movieName, Pageable pageable);

    Page<MovieDTO> findMoviesThatHaveFeedback(Pageable pageable);

    MovieDTO toDTO(Movie movie);

    Movie toEntity(MovieDTO dto);

    List<MovieDTO> findAllShowingMovies();

    List<MovieDTO> searchExistingMovieByName(String name);

    Page<MovieDTO> findMoviesByTheaterAndGenreAndKeywordAndDateRange(Integer theaterId, Integer genreId,
                                                                     String keyword, Movie_Status status,
                                                                     Theater_Status theaterStatus,
                                                                     LocalDateTime startDate, LocalDateTime endDate,
                                                                     List<AgeLimit> ageLimits,
                                                                     Pageable pageable);

    Page<MovieDTO> findMoviesByTheaterAndKeywordAndDateRange(Integer theaterId, String keyword,
                                                             Movie_Status status, Theater_Status theaterStatus,
                                                             LocalDateTime startDate, LocalDateTime endDate,
                                                             List<AgeLimit> ageLimits,
                                                             Pageable pageable);

    Page<MovieDTO> findMoviesByTheaterAndGenreAndDateRange(Integer theaterId, Integer genreId,
                                                           Movie_Status status, Theater_Status theaterStatus,
                                                           LocalDateTime startDate, LocalDateTime endDate,
                                                           List<AgeLimit> ageLimits,
                                                           Pageable pageable);

    Page<MovieDTO> findMoviesByTheaterAndDateRange(Integer theaterId, Movie_Status status,
                                                   Theater_Status theaterStatus,
                                                   LocalDateTime startDate, LocalDateTime endDate,
                                                   List<AgeLimit> ageLimits,
                                                   Pageable pageable);

    MovieDTO findById(Integer movieID);
    public Page<MovieDTO> findShowingMoviesWithFilters(String name, Integer genreId, String startDateStr, String endDateStr, int page, int size);


    List<LocalDate> getAvailableScheduleDatesForCashier(Integer theaterId, LocalDateTime startDate, LocalDateTime endDate);

    Page<MovieDTO> findMoviesForCashierByAllFilters(Integer theaterId, Integer genreId,
                                                    String keyword, Movie_Status status,
                                                    Theater_Status theaterStatus,
                                                    LocalDateTime startDate, LocalDateTime endDate,
                                                    List<AgeLimit> ageLimits,
                                                    Pageable pageable);

    Page<MovieDTO> findMoviesForCashierByKeyword(Integer theaterId, String keyword,
                                                 Movie_Status status, Theater_Status theaterStatus,
                                                 LocalDateTime startDate, LocalDateTime endDate,
                                                 List<AgeLimit> ageLimits,
                                                 Pageable pageable);

    Page<MovieDTO> findMoviesForCashierByGenre(Integer theaterId, Integer genreId,
                                               Movie_Status status, Theater_Status theaterStatus,
                                               LocalDateTime startDate, LocalDateTime endDate,
                                               List<AgeLimit> ageLimits,
                                               Pageable pageable);

    Page<MovieDTO> findMoviesForCashier(Integer theaterId, Movie_Status status,
                                        Theater_Status theaterStatus,
                                        LocalDateTime startDate, LocalDateTime endDate,
                                        List<AgeLimit> ageLimits,
                                        Pageable pageable);

    Page<MovieDTO> findMoviesByScheduleAndTheaterAndRoomTypeAndGenre(LocalDateTime schedule, int theaterId, String roomType, String genreName);

    Page<MovieDTO> findMoviesByScheduleAndTheater(LocalDateTime schedule, String theaterName);

    List<MovieDTO> getMoviesByActor(String actorName);
}