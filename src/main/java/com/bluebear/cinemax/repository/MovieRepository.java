package com.bluebear.cinemax.repository;

import com.bluebear.cinemax.entity.Genre;
import com.bluebear.cinemax.entity.Movie;
import com.bluebear.cinemax.entity.Schedule;
import com.bluebear.cinemax.enumtype.Movie_Status;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface MovieRepository extends JpaRepository<Movie, Integer> {
    //find by condition

    @Query("SELECT m FROM Movie m JOIN m.genres g WHERE g.genreID = :genreId")
    List<Movie> findByGenreIdAndStatus(Integer genreId, Movie_Status status, Pageable pageable);

    List<Movie> findByGenresAndStatusOrderByMovieRateDesc(Genre genre, Movie_Status status, Pageable pageable);

    List<Movie> findBymovieNameContainingIgnoreCaseAndStatus(String movieName, Movie_Status status, Pageable pageable);

    List<Movie> findByGenresAndMovieNameContainingIgnoreCaseAndStatus(Genre genre, String movieName, Movie_Status status, Pageable pageable);

    Movie findTop1ByStatusOrderByMovieRateDesc(Movie_Status status);

    List<Movie> findMoviesByGenresAndMovieNameContainingIgnoreCaseAndStatusOrderByMovieRateDesc(Genre genre, String movieName, Movie_Status status, Pageable pageable);

    List<Movie> findMovesByMovieNameContainingIgnoreCaseAndStatusOrderByMovieRateDesc(String movieName, Movie_Status status, Pageable pageable);

    List<Movie> findTop3ByStatusOrderByMovieRateDesc(Movie_Status status);

    List<Movie> findMoviesByStartDateBeforeAndEndDateAfterOrderByMovieRateDesc(Date currentDate, Date nowDate);

    List<Movie> findMoviesByStartDateAfter(Date currentDate);

    @Query("SELECT DISTINCT s.movie FROM Schedule s WHERE CAST(s.startTime AS DATE) = CAST(:today AS DATE)")
    List<Movie> findMoviesWithScheduleToday(Date today);

    @Query("SELECT DISTINCT s.movie FROM Schedule s JOIN s.room r JOIN r.theater t WHERE t.theaterID = :theaterId AND CAST(s.startTime AS DATE) = CAST(:today AS DATE)")
    List<Movie> findMoviesWithScheduleTodayWithTheater(int theaterId, Date today);
    //count

    int countMovieByStatus(Movie_Status status);

    int countMovieByGenresAndStatus(Genre genre, Movie_Status status);

    int countMovieByMovieNameContainingAndStatus(String movieName, Movie_Status status);

    int countMovieByGenresAndMovieNameContainingAndStatus(Genre genre, String movieName, Movie_Status status);

    //find all

    List<Movie> findAllByStatus(Movie_Status status, Pageable pageable);

    List<Movie> findAllByStatusOrderByMovieRateDesc(Movie_Status status, Pageable pageable);
}
