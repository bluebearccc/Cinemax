package com.bluebear.cinemax.repository;

import com.bluebear.cinemax.entity.Schedule;
import com.bluebear.cinemax.enumtype.Schedule_Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface ScheduleRepository extends JpaRepository<Schedule, Integer> {
    List<Schedule> findByStatus(Schedule_Status status);

    @Query("SELECT s FROM Movie m JOIN m.scheduleList s WHERE m.movieID = :movieId AND CAST(s.startTime AS DATE) = CAST(:day AS DATE)")
    List<Schedule> findSchedulesByMovie_MovieIDInToday(int movieId, Date day);
}
