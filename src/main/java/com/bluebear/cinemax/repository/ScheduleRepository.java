package com.bluebear.cinemax.repository;

import com.bluebear.cinemax.entity.Schedule;
import com.bluebear.cinemax.enumtype.Schedule_Status;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface ScheduleRepository extends JpaRepository<Schedule, Integer> {
    Page<Schedule> findByStatus(Schedule_Status status, Pageable pageable);

    @Query("SELECT s FROM Movie m JOIN m.scheduleList s WHERE m.movieID = :movieId AND CAST(s.startTime AS DATE) = CAST(:day AS DATE) AND s.startTime > :day AND s.status = :status")
    Page<Schedule> findSchedulesByMovie_MovieIDInTodayAndStatus(int movieId, LocalDateTime day, Schedule_Status status, Pageable pageable);

    @Query("SELECT s FROM Movie m JOIN m.scheduleList s JOIN s.room r JOIN r.theater t WHERE m.movieID = :movieId AND t.theaterID = :theaterId AND CAST(s.startTime AS DATE) = CAST(:day AS DATE) AND s.startTime > :day AND LOWER(r.typeOfRoom) = LOWER(:roomType) AND s.status = :status")
    Page<Schedule> findSchedulesByMovie_MovieIDAndTheaterAndDayAndRoomTypeStatus(int movieId, int theaterId, LocalDateTime day, Schedule_Status status, String roomType, Pageable pageable);

}
