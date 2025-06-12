package com.bluebear.cinemax.repository.cashier;

import com.bluebear.cinemax.entity.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ScheduleRepository extends JpaRepository<Schedule, Integer> {

    List<Schedule> findByMovie_MovieIdAndStatus(Integer movieId, Schedule.ScheduleStatus status);

    @Query("SELECT s FROM Schedule s WHERE s.movie.movieId = :movieId AND s.status = :status AND CAST(s.startTime AS date) = :date ORDER BY s.startTime")
    List<Schedule> findByMovieAndDate(
            @Param("movieId") Integer movieId,
            @Param("status") Schedule.ScheduleStatus status,
            @Param("date") LocalDate date
    );

    @Query("SELECT s FROM Schedule s WHERE s.movie.movieId = :movieId " +
            "AND s.status = :status " +
            "AND s.startTime >= :currentDateTime " +
            "AND CAST(s.startTime AS DATE) = :currentDate " +
            "ORDER BY s.startTime ASC")
    List<Schedule> findAvailableSchedulesToday(
            @Param("movieId") Integer movieId,
            @Param("status") Schedule.ScheduleStatus status,
            @Param("currentDateTime") LocalDateTime currentDateTime,
            @Param("currentDate") LocalDate currentDate
    );

    @Query("SELECT s FROM Schedule s WHERE s.movie.movieId = :movieId " +
            "AND s.status = :status " +
            "AND s.startTime >= :startOfDay " +
            "AND s.startTime < :startOfNextDay " +
            "AND s.startTime >= :currentDateTime " +
            "ORDER BY s.startTime ASC")
    List<Schedule> findAvailableSchedulesTodayByRange(
            @Param("movieId") Integer movieId,
            @Param("status") Schedule.ScheduleStatus status,
            @Param("currentDateTime") LocalDateTime currentDateTime,
            @Param("startOfDay") LocalDateTime startOfDay,
            @Param("startOfNextDay") LocalDateTime startOfNextDay
    );
}