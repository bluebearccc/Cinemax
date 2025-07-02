package com.bluebear.cinemax.repository;

import com.bluebear.cinemax.entity.Genre;
import com.bluebear.cinemax.entity.Movie;
import com.bluebear.cinemax.entity.Schedule;
import com.bluebear.cinemax.enumtype.Movie_Status;
import com.bluebear.cinemax.enumtype.Theater_Status;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Repository
public interface MovieRepository extends JpaRepository<Movie, Integer> {

    @Query(value="select * from Movie m where m.StartDate <= GETDATE() And m.EndDate >= GETDATE()", nativeQuery = true)
    List<Movie> findAllShowingMovies();

    @Query(value="select * from Movie m where LOWER(m.MovieName) LIKE LOWER(CONCAT('%', :name, '%')) AND m.StartDate <= GETDATE() And m.EndDate >= GETDATE()", nativeQuery = true)
    List<Movie> findAllByMovieName(@Param("name") String name);

    //=========================================cashier
    // 1. Lấy theo theaterId + status + theaterStatus + khoảng ngày
    @Query("""
    SELECT DISTINCT m FROM Movie m
    JOIN m.scheduleList s
    JOIN s.room r
    JOIN r.theater t
    WHERE t.theaterID = :theaterId
    AND m.status = :status
    AND t.status = :theaterStatus
    AND s.startTime BETWEEN :startDate AND :endDate
    """)
    Page<Movie> findByTheaterIdAndDateRange(
            @Param("theaterId") Integer theaterId,
            @Param("status") Movie_Status status,
            @Param("theaterStatus") Theater_Status theaterStatus,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable
    );

    // 2. Lọc theo genre + theater + khoảng ngày
    @Query("""
        SELECT DISTINCT m FROM Movie m
        JOIN m.genres g
        JOIN m.scheduleList s
        JOIN s.room r
        JOIN r.theater t
        WHERE g.genreID = :genreId
        AND t.theaterID = :theaterId
        AND m.status = :status
        AND t.status = :theaterStatus
        AND s.startTime BETWEEN :startDate AND :endDate
    """)
    Page<Movie> findByTheaterIdAndGenreIdAndDateRange(
            @Param("theaterId") Integer theaterId,
            @Param("genreId") Integer genreId,
            @Param("status") Movie_Status status,
            @Param("theaterStatus") Theater_Status theaterStatus,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable
    );

    // 3. Lọc theo keyword + theater + khoảng ngày
    @Query("""
        SELECT DISTINCT m FROM Movie m
        JOIN m.scheduleList s
        JOIN s.room r
        JOIN r.theater t
        WHERE t.theaterID = :theaterId
        AND LOWER(m.movieName) LIKE LOWER(CONCAT('%', :keyword, '%'))
        AND m.status = :status
        AND t.status = :theaterStatus
        AND s.startTime BETWEEN :startDate AND :endDate
    """)
    Page<Movie> findByTheaterIdAndKeywordAndDateRange(
            @Param("theaterId") Integer theaterId,
            @Param("keyword") String keyword,
            @Param("status") Movie_Status status,
            @Param("theaterStatus") Theater_Status theaterStatus,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable
    );

    // 4. Lọc theo genre + keyword + theater + khoảng ngày
    @Query("""
                SELECT DISTINCT m FROM Movie m
                JOIN m.genres g
                JOIN m.scheduleList s
                JOIN s.room r
                JOIN r.theater t
                WHERE g.genreID = :genreId
                AND t.theaterID = :theaterId
                AND LOWER(m.movieName) LIKE LOWER(CONCAT('%', :keyword, '%'))
                AND m.status = :status
                AND t.status = :theaterStatus
                AND s.startTime BETWEEN :startDate AND :endDate
            """)
    Page<Movie> findByTheaterIdAndGenreIdAndKeywordAndDateRange(
            @Param("theaterId") Integer theaterId,
            @Param("genreId") Integer genreId,
            @Param("keyword") String keyword,
            @Param("status") Movie_Status status,
            @Param("theaterStatus") Theater_Status theaterStatus,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable
    );
    @Query("""
        SELECT DISTINCT m FROM Movie m LEFT JOIN m.genres g
        WHERE (m.endDate >= :currentDate AND m.startDate <= :currentDate)
        AND (:name IS NULL OR m.movieName LIKE %:name%)
        AND (:genreId IS NULL OR g.genreID = :genreId)
        AND (:startDate IS NULL OR m.endDate >= :startDate)
        AND (:endDate IS NULL OR m.startDate <= :endDate)
    """)
    Page<Movie> findShowingMoviesWithFilters(
            @Param("currentDate") LocalDateTime currentDate,
            @Param("name") String name,
            @Param("genreId") Integer genreId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable
    );
}
