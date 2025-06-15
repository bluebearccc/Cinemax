package com.bluebear.cinemax.repository.cashier;

import com.bluebear.cinemax.entity.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ScheduleRepository extends JpaRepository<Schedule, Integer> {

    @Query("""
    SELECT s FROM Schedule s
    WHERE s.movie.movieId = :movieId
    AND s.status = :status
    AND CAST(s.startTime AS date) = :date
    AND s.room.theater.theaterId = :theaterId
    ORDER BY s.startTime
    """)
    List<Schedule> findByMovieAndDateAndTheater(
            @Param("movieId") Integer movieId,
            @Param("status") Schedule.ScheduleStatus status,
            @Param("date") LocalDate date,
            @Param("theaterId") Integer theaterId
    );

}