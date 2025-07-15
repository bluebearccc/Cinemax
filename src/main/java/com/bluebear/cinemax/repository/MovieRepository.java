package com.bluebear.cinemax.repository;
import com.bluebear.cinemax.dto.Movie.MovieRevenueDTO;
import com.bluebear.cinemax.entity.Movie;
import com.bluebear.cinemax.enumtype.Movie_Status;
import org.springframework.boot.autoconfigure.data.web.SpringDataWebProperties;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MovieRepository extends JpaRepository<Movie, Long> {
    List<Movie> findByMovieNameContaining(String movieName);
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
