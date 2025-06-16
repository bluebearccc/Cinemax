package com.bluebear.cinemax.service.staff;

import com.bluebear.cinemax.entity.Schedule;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

public interface ScheduleService {
    public List<Schedule> findByMovieId(Integer movieId);
    public List<Schedule> findAllScheduleByMovieIdAndRoomIdAndDate(Integer movieId, Integer roomId, LocalDate date);
    public List<Schedule> findOverlappingSchedules(Integer roomId, LocalDateTime newStartTime, LocalDateTime newEndTime);
    public void saveSchedule(Schedule schedule);
}
