package com.bluebear.cinemax.repository.cashier;

import com.bluebear.cinemax.entity.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ScheduleRepository extends JpaRepository<Schedule, Integer> {

    @Query("""
    SELECT s FROM Schedule s 
    JOIN s.room r 
    WHERE s.movie.movieId = :movieId 
    AND s.status = :status 
    AND s.startTime >= :startOfDay
    AND s.startTime < :startOfNextDay
    AND r.theater.theaterId = :theaterId 
    ORDER BY s.startTime
""")
    List<Schedule> findByMovieAndDateRangeAndTheater(
            @Param("movieId") Integer movieId,
            @Param("status") Schedule.ScheduleStatus status,
            @Param("startOfDay") LocalDateTime startOfDay,
            @Param("startOfNextDay") LocalDateTime startOfNextDay,
            @Param("theaterId") Integer theaterId
    );


}