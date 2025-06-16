package com.bluebear.cinemax.repository.staff;

import com.bluebear.cinemax.entity.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Repository
public interface ScheduleRepository extends JpaRepository<Schedule, Integer> {
    List<Schedule> findByMovie_MovieId(Integer movieId);
    @Query(value = "SELECT * FROM Schedule s WHERE s.MovieID = :movieId AND s.RoomID = :roomId AND CAST(s.EndTime AS DATE) = :date", nativeQuery = true)
    List<Schedule> findAllByMovieIdAndRoomId(@Param("movieId") Integer movieId, @Param("roomId") Integer roomId, @Param("date") LocalDate date);
    @Query(value = "SELECT s FROM Schedule s WHERE s.RoomID = :roomId " +
            "AND s.startTime < :newEndTime " +
            "AND s.endTime > :newStartTime", nativeQuery = true)
    List<Schedule> findOverlappingSchedules(@Param("roomId") Integer roomId,
                                            @Param("newStartTime") LocalDateTime newStartTime,
                                            @Param("newEndTime") LocalDateTime newEndTime);
}
