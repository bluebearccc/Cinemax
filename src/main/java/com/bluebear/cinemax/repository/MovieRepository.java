package com.bluebear.cinemax.repository;

import com.bluebear.cinemax.entity.Genre;
import com.bluebear.cinemax.entity.Movie;
import com.bluebear.cinemax.enumtype.Movie_Status;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MovieRepository extends JpaRepository<Movie, Integer> {
    @Query("SELECT m FROM Movie m JOIN m.genres g WHERE g.genreID = :genreId")
    Page<Movie> findByGenreIdAndStatus(Integer genreId, Movie_Status status, Pageable pageable);

    Page<Movie> findBymovieNameContainingIgnoreCaseAndStatus(String movieName, Movie_Status status, Pageable pageable);

    Page<Movie> findByGenresAndMovieNameContainingIgnoreCaseAndStatus(Genre genre, String movieName, Movie_Status status, Pageable pageable);

    Movie findTop1ByStatusOrderByMovieRateDesc(Movie_Status status);

    List<Movie> findMoviesByActors_ActorNameIgnoreCaseAndStatus(String actorName, Movie_Status status);

    @Query("SELECT m FROM Movie m WHERE m.status = :status AND CAST(m.startDate AS DATE) <= CAST(:today AS DATE) ORDER BY m.movieRate desc")
    Page<Movie> findTopCurrentlyShowByStatusOrderByMovieRateDesc(Movie_Status status, LocalDateTime today, Pageable pageable);

    Page<Movie> findMoviesByStartDateBeforeAndEndDateAfterAndStatus(LocalDateTime currentDate, LocalDateTime nowDate, Pageable pageable, Movie_Status status);

    Page<Movie> findMoviesByStartDateAfterAndStatus(LocalDateTime currentDate, Pageable pageable, Movie_Status status);

    @Query("SELECT DISTINCT s.movie FROM Schedule s WHERE CAST(s.startTime AS DATE) = CAST(:today AS DATE) AND s.movie.status = :status")
    Page<Movie> findMoviesWithScheduleToday(LocalDateTime today, Pageable pageable, Movie_Status status);

    @Query("SELECT DISTINCT s.movie FROM Schedule s JOIN s.room r JOIN r.theater t WHERE t.theaterID = :theaterId AND LOWER(r.typeOfRoom) = LOWER(:roomType) AND CAST(s.startTime AS DATE) = CAST(:today AS DATE) AND s.movie.status = :status")
    Page<Movie> findMoviesWithScheduleTodayWithTheaterAndRoomType(int theaterId, LocalDateTime today, String roomType, Pageable pageable, Movie_Status status);

    @Query("SELECT DISTINCT s.movie FROM Schedule s JOIN s.room r JOIN r.theater t JOIN s.movie.genres g WHERE t.theaterID = :theaterId AND LOWER(r.typeOfRoom) = LOWER(:roomType) AND CAST(s.startTime AS DATE) = CAST(:today AS DATE) AND s.movie.status = :status AND g.genreName = :genrename")
    Page<Movie> findMoviesWithScheduleTodayWithTheaterAndRoomTypeAndGenre(int theaterId, LocalDateTime today, String roomType, Pageable pageable, Movie_Status status, String genrename);


    @Query("SELECT DISTINCT s.movie FROM Schedule s JOIN s.room r JOIN r.theater t WHERE t.theaterID = :theaterId AND CAST(s.startTime AS DATE) = CAST(:today AS DATE) AND s.movie.status = :status")
    Page<Movie> findMoviesWithScheduleTodayWithTheater(int theaterId, LocalDateTime today, Pageable pageable, Movie_Status status);

    //Movie-Controller-Filter
    @Query("SELECT m FROM Movie m WHERE LOWER(m.movieName) LIKE LOWER(CONCAT('%', :movieName, '%')) AND m.status = :status")
    Page<Movie> findMoviesByMovieNameAndStatus(String movieName, Movie_Status status, Pageable pageable);

    @Query("SELECT DISTINCT m FROM Movie m JOIN m.scheduleList s JOIN s.room r JOIN r.theater t WHERE t.theaterID = :theaterId AND LOWER(m.movieName) LIKE LOWER(CONCAT('%', :movieName, '%')) AND s.startTime > :now AND m.status = :status")
    Page<Movie> findMoviesByTheaterIdAndMovieNameAndStatus(int theaterId, String movieName, Movie_Status status, LocalDateTime now, Pageable pageable);

    @Query("SELECT DISTINCT m FROM Movie m JOIN m.genres g WHERE g.genreID = :genreId AND LOWER(m.movieName) LIKE LOWER(CONCAT('%', :movieName, '%')) AND m.status = :status")
    Page<Movie> findMoviesByGenreIdAndMovieNameAndStatus(int genreId, String movieName, Movie_Status status, Pageable pageable);

    @Query("SELECT DISTINCT m FROM Movie m JOIN m.genres g JOIN m.scheduleList s JOIN s.room r JOIN r.theater t WHERE t.theaterID = :theaterId AND g.genreID = :genreId AND LOWER(m.movieName) LIKE LOWER(CONCAT('%', :movieName, '%')) AND s.startTime > :now AND m.status = :status")
    Page<Movie> findMoviesByTheaterIdAndGenreIdAndMovieNameAndStatus(int theaterId, int genreId, String movieName, Movie_Status status, LocalDateTime now, Pageable pageable);

    @Query("SELECT DISTINCT m FROM Movie m JOIN m.feedbackList mf WHERE m.status = :status")
    Page<Movie> findMoviesThatHaveFeedback(Movie_Status status, Pageable pageable);
    //find all
    Page<Movie> findAllByStatus(Movie_Status status, Pageable pageable);
}
