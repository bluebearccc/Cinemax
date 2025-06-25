package com.bluebear.cinemax.repository;

import com.bluebear.cinemax.entity.Schedule;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface ScheduleRepository extends JpaRepository<Schedule, Integer> {

    @Query("""
                SELECT s FROM Schedule s
                JOIN s.movie m
                JOIN s.room r
                JOIN r.theater t
                WHERE t.theaterID = :theaterId
                AND m.movieID = :movieId
                AND s.startTime BETWEEN :startDate AND :endDate
                ORDER BY s.startTime ASC
            """)
    Page<Schedule> findSchedulesByMovieIdAndTheaterIdAndDateRange(
            @Param("theaterId") Integer theaterId,
            @Param("movieId") Integer movieId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable
    );

}