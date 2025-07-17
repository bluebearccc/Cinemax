package com.bluebear.cinemax.repository;

import com.bluebear.cinemax.dto.Movie.MovieRevenueDTO;
import com.bluebear.cinemax.dto.MovieDTO;
import com.bluebear.cinemax.entity.Genre;
import com.bluebear.cinemax.entity.Movie;
import com.bluebear.cinemax.entity.Schedule;
import com.bluebear.cinemax.enumtype.Age_Limit;
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
    @Query("SELECT m FROM Movie m JOIN m.genres g WHERE g.genreID = :genreId")
    Page<Movie> findByGenreIdAndStatus(Integer genreId, Movie_Status status, Pageable pageable);

    Page<Movie> findBymovieNameContainingIgnoreCaseAndStatus(String movieName, Movie_Status status, Pageable pageable);

    Page<Movie> findByGenresAndMovieNameContainingIgnoreCaseAndStatus(Genre genre, String movieName, Movie_Status status, Pageable pageable);

    Movie findTop1ByStatusOrderByMovieRateDesc(Movie_Status status);

    @Query("SELECT m FROM Movie m WHERE m.status = :status AND CAST(m.startDate AS DATE) <= CAST(:today AS DATE) ORDER BY m.movieRate desc")
    Page<Movie> findTopCurrentlyShowByStatusOrderByMovieRateDesc(Movie_Status status, LocalDateTime today, Pageable pageable);

    @Query(value="select * from Movie m where m.StartDate <= GETDATE() And m.EndDate >= GETDATE()", nativeQuery = true)
    List<Movie> findAllShowingMovies();
    Page<Movie> findMoviesByStartDateBeforeAndEndDateAfter(LocalDateTime currentDate, LocalDateTime nowDate, Pageable pageable);

    @Query(value="select * from Movie m where LOWER(m.MovieName) LIKE LOWER(CONCAT('%', :name, '%')) AND m.StartDate <= GETDATE() And m.EndDate >= GETDATE()", nativeQuery = true)
    List<Movie> findAllByMovieName(@Param("name") String name);
    Page<Movie> findMoviesByStartDateAfter(LocalDateTime currentDate, Pageable pageable);

    // ==================== [PHẦN ĐƯỢC SỬA] - Bắt đầu ====================
    // Sửa các truy vấn để dùng "IN :ageLimits" thay vì "= :ageLimit"

    @Query("""
    SELECT DISTINCT m FROM Movie m
    JOIN m.scheduleList s
    JOIN s.room r
    JOIN r.theater t
    WHERE t.theaterID = :theaterId
    AND m.status = :status
    AND t.status = :theaterStatus
    AND s.startTime BETWEEN :startDate AND :endDate
    AND (:ageLimits IS NULL OR m.ageLimit IN :ageLimits)
    """)
    Page<Movie> findByTheaterIdAndDateRange(
            @Param("theaterId") Integer theaterId,
            @Param("status") Movie_Status status,
            @Param("theaterStatus") Theater_Status theaterStatus,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("ageLimits") List<Age_Limit> ageLimits,
            Pageable pageable
    );
    @Query("SELECT DISTINCT s.movie FROM Schedule s WHERE CAST(s.startTime AS DATE) = CAST(:today AS DATE)")
    Page<Movie> findMoviesWithScheduleToday(LocalDateTime today, Pageable pageable);

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
        AND (:ageLimits IS NULL OR m.ageLimit IN :ageLimits)
    """)
    Page<Movie> findByTheaterIdAndGenreIdAndDateRange(
            @Param("theaterId") Integer theaterId,
            @Param("genreId") Integer genreId,
            @Param("status") Movie_Status status,
            @Param("theaterStatus") Theater_Status theaterStatus,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("ageLimits") List<Age_Limit> ageLimits,
            Pageable pageable
    );
    @Query("SELECT DISTINCT s.movie FROM Schedule s JOIN s.room r JOIN r.theater t WHERE t.theaterID = :theaterId AND LOWER(r.typeOfRoom) = LOWER(:roomType) AND CAST(s.startTime AS DATE) = CAST(:today AS DATE)")
    Page<Movie> findMoviesWithScheduleTodayWithTheaterAndRoomType(int theaterId, LocalDateTime today, String roomType, Pageable pageable);

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
        AND (:ageLimits IS NULL OR m.ageLimit IN :ageLimits)
    """)
    Page<Movie> findByTheaterIdAndKeywordAndDateRange(
            @Param("theaterId") Integer theaterId,
            @Param("keyword") String keyword,
            @Param("status") Movie_Status status,
            @Param("theaterStatus") Theater_Status theaterStatus,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("ageLimits") List<Age_Limit> ageLimits,
            Pageable pageable
    );

    @Query("SELECT m FROM Movie m WHERE LOWER(m.movieName) LIKE LOWER(CONCAT('%', :movieName, '%')) AND m.status = :status")
    Page<Movie> findMoviesByMovieNameAndStatus(String movieName, Movie_Status status, Pageable pageable);

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
        AND (:ageLimits IS NULL OR m.ageLimit IN :ageLimits)
    """)
    Page<Movie> findByTheaterIdAndGenreIdAndKeywordAndDateRange(
            @Param("theaterId") Integer theaterId,
            @Param("genreId") Integer genreId,
            @Param("keyword") String keyword,
            @Param("status") Movie_Status status,
            @Param("theaterStatus") Theater_Status theaterStatus,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("ageLimits") List<Age_Limit> ageLimits,
            Pageable pageable
    );
    // ==================== [PHẦN ĐƯỢC SỬA] - Kết thúc ====================

    @Query("SELECT DISTINCT m FROM Movie m JOIN m.scheduleList s JOIN s.room r JOIN r.theater t WHERE t.theaterID = :theaterId AND LOWER(m.movieName) LIKE LOWER(CONCAT('%', :movieName, '%')) AND s.startTime > :now AND m.status = :status")
    Page<Movie> findMoviesByTheaterIdAndMovieNameAndStatus(int theaterId, String movieName, Movie_Status status, LocalDateTime now, Pageable pageable);

    @Query("SELECT DISTINCT m FROM Movie m JOIN m.genres g WHERE g.genreID = :genreId AND LOWER(m.movieName) LIKE LOWER(CONCAT('%', :movieName, '%')) AND m.status = :status")
    Page<Movie> findMoviesByGenreIdAndMovieNameAndStatus(int genreId, String movieName, Movie_Status status, Pageable pageable);

    @Query("SELECT DISTINCT m FROM Movie m JOIN m.genres g JOIN m.scheduleList s JOIN s.room r JOIN r.theater t WHERE t.theaterID = :theaterId AND g.genreID = :genreId AND LOWER(m.movieName) LIKE LOWER(CONCAT('%', :movieName, '%')) AND s.startTime > :now AND m.status = :status")
    Page<Movie> findMoviesByTheaterIdAndGenreIdAndMovieNameAndStatus(int theaterId, int genreId, String movieName, Movie_Status status, LocalDateTime now, Pageable pageable);

    @Query("SELECT DISTINCT m FROM Movie m JOIN m.feedbackList mf WHERE m.status = :status")
    Page<Movie> findMoviesThatHaveFeedback(Movie_Status status, Pageable pageable);

    Page<Movie> findAllByStatus(Movie_Status status, Pageable pageable);

    List<Movie> findByMovieNameContaining(String keyword);

    MovieDTO getMovieByMovieID(Integer movieID);

    //my

    long countByStatus(Movie_Status status);
    @Query("SELECT COUNT(m) FROM Movie m WHERE m.status = 'ACTIVE' AND m.startDate <= :now AND m.endDate >= :now")
    Integer countCurrentlyShowing(@Param("now") LocalDateTime now);
    @Query("""
    SELECT new com.bluebear.cinemax.dto.Movie.MovieRevenueDTO(
     m.movieID, m.movieName, m.image,
             COUNT(ds),
             CAST(COALESCE(SUM(i.totalPrice), 0) - COALESCE(SUM(fd.totalPrice), 0)AS DOUBLE ),
             m.startDate, m.endDate)
    FROM Movie m
        LEFT JOIN Schedule s ON s.movie = m
        LEFT JOIN DetailSeat ds ON ds.schedule = s
        LEFT JOIN Invoice i ON i = ds.invoice AND i.status = 'Booked'
        LEFT JOIN Detail_FD fd ON fd.invoice = i
        WHERE m.status = 'ACTIVE'
          AND (
               :filter IS NULL
               OR (:filter = 'SHOWING' AND m.startDate <= CURRENT_TIMESTAMP AND m.endDate >= CURRENT_TIMESTAMP)
               OR (:filter = 'COMING_SOON' AND m.startDate > CURRENT_TIMESTAMP)
          )
        GROUP BY m.movieID, m.movieName, m.image, m.startDate, m.endDate
        ORDER BY\s
            CASE WHEN (SUM(i.totalPrice) - SUM(fd.totalPrice)) IS NULL THEN 1 ELSE 0 END,
            (SUM(i.totalPrice) - SUM(fd.totalPrice)) DESC
    """)
    Page<MovieRevenueDTO> getMovieStatistics(@Param("filter") String filter, Pageable pageable);

    @Query("""
    SELECT new com.bluebear.cinemax.dto.Movie.MovieRevenueDTO(
     m.movieID, m.movieName, m.image,
     COUNT(ds),
     CAST(COALESCE(SUM(i.totalPrice), 0) AS DOUBLE),
     m.startDate, m.endDate)
    FROM Movie m
    LEFT JOIN Schedule s ON s.movie = m
    LEFT JOIN DetailSeat ds ON ds.schedule = s
    LEFT JOIN Invoice i ON i = ds.invoice AND i.status = 'PAID'
    WHERE m.status = 'ACTIVE'
      AND LOWER(m.movieName) LIKE %:keyword%
      AND (
           :filter IS NULL
           OR (:filter = 'SHOWING' AND m.startDate <= CURRENT_TIMESTAMP AND m.endDate >= CURRENT_TIMESTAMP)
           OR (:filter = 'COMING_SOON' AND m.startDate > CURRENT_TIMESTAMP)
      )
    GROUP BY m.movieID, m.movieName, m.image, m.startDate, m.endDate
    ORDER BY SUM(i.totalPrice) DESC NULLS LAST
""")
    Page<MovieRevenueDTO> getMovieStatisticsWithKeyword(@Param("filter") String filter,
                                                        @Param("keyword") String keyword,
                                                        Pageable pageable);
}