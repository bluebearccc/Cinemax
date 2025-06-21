package com.bluebear.cinemax.repository.cashier;

import com.bluebear.cinemax.entity.Movie;
import com.bluebear.cinemax.entity.Theater;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface MovieRepository extends JpaRepository<Movie, Integer> {

    // 1. Lấy theo theaterId + status + theaterStatus + ngày
    @Query("""
    SELECT DISTINCT m FROM Movie m 
    JOIN m.schedules s 
    JOIN s.room r 
    JOIN r.theater t
    WHERE t.theaterId = :theaterId 
    AND m.status = :status 
    AND t.status = :theaterStatus
    AND s.startTime >= :startOfDay
    AND s.startTime < :startOfNextDay
    """)
    Page<Movie> findByTheaterIdAndDate(
            @Param("theaterId") Integer theaterId,
            @Param("status") Movie.MovieStatus status,
            @Param("theaterStatus") Theater.TheaterStatus theaterStatus,
            @Param("startOfDay") LocalDateTime startOfDay,
            @Param("startOfNextDay") LocalDateTime startOfNextDay,
            Pageable pageable
    );

    // 2. Lọc theo genre + theater + ngày - FIXED
    @Query("""
        SELECT DISTINCT m FROM Movie m 
        JOIN MovieGenre mg ON mg.movie.movieId = m.movieId 
        JOIN Genre g ON g.genreId = mg.genre.genreId 
        JOIN Schedule s ON s.movie.movieId = m.movieId 
        JOIN Room r ON r.roomId = s.room.roomId 
        JOIN Theater t ON t.theaterId = r.theater.theaterId
        WHERE g.genreId = :genreId 
        AND t.theaterId = :theaterId 
        AND m.status = :status 
        AND t.status = :theaterStatus
        AND s.startTime >= :startOfDay
        AND s.startTime < :startOfNextDay
    """)
    Page<Movie> findByTheaterIdAndGenreIdAndDate(
            @Param("theaterId") Integer theaterId,
            @Param("genreId") Integer genreId,
            @Param("status") Movie.MovieStatus status,
            @Param("theaterStatus") Theater.TheaterStatus theaterStatus,
            @Param("startOfDay") LocalDateTime startOfDay,
            @Param("startOfNextDay") LocalDateTime startOfNextDay,
            Pageable pageable
    );

    // 3. Lọc theo keyword + theater + ngày - FIXED
    @Query("""
        SELECT DISTINCT m FROM Movie m 
        JOIN Schedule s ON s.movie.movieId = m.movieId 
        JOIN Room r ON r.roomId = s.room.roomId 
        JOIN Theater t ON t.theaterId = r.theater.theaterId
        WHERE t.theaterId = :theaterId 
        AND LOWER(m.movieName) LIKE LOWER(CONCAT('%', :keyword, '%')) 
        AND m.status = :status 
        AND t.status = :theaterStatus
        AND s.startTime >= :startOfDay
        AND s.startTime < :startOfNextDay
    """)
    Page<Movie> findByTheaterIdAndKeywordAndDate(
            @Param("theaterId") Integer theaterId,
            @Param("keyword") String keyword,
            @Param("status") Movie.MovieStatus status,
            @Param("theaterStatus") Theater.TheaterStatus theaterStatus,
            @Param("startOfDay") LocalDateTime startOfDay,
            @Param("startOfNextDay") LocalDateTime startOfNextDay,
            Pageable pageable
    );

    // 4. Lọc theo genre + keyword + theater + ngày - FIXED
    @Query("""
        SELECT DISTINCT m FROM Movie m 
        JOIN MovieGenre mg ON mg.movie.movieId = m.movieId 
        JOIN Genre g ON g.genreId = mg.genre.genreId 
        JOIN Schedule s ON s.movie.movieId = m.movieId 
        JOIN Room r ON r.roomId = s.room.roomId 
        JOIN Theater t ON t.theaterId = r.theater.theaterId
        WHERE g.genreId = :genreId 
        AND t.theaterId = :theaterId 
        AND LOWER(m.movieName) LIKE LOWER(CONCAT('%', :keyword, '%')) 
        AND m.status = :status 
        AND t.status = :theaterStatus
        AND s.startTime >= :startOfDay
        AND s.startTime < :startOfNextDay
    """)
    Page<Movie> findByTheaterIdAndGenreIdAndKeywordAndDate(
            @Param("theaterId") Integer theaterId,
            @Param("genreId") Integer genreId,
            @Param("keyword") String keyword,
            @Param("status") Movie.MovieStatus status,
            @Param("theaterStatus") Theater.TheaterStatus theaterStatus,
            @Param("startOfDay") LocalDateTime startOfDay,
            @Param("startOfNextDay") LocalDateTime startOfNextDay,
            Pageable pageable
    );
}