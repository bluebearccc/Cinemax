package com.bluebear.cinemax.service.staff;

import com.bluebear.cinemax.dto.ScheduleDTO;
import com.bluebear.cinemax.entity.Schedule;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface ScheduleService {
    public List<ScheduleDTO> findByMovieId(Integer movieId);
    public List<ScheduleDTO> findAllScheduleByMovieIdAndRoomIdAndDate(Integer movieId, Integer roomId, LocalDate date);
    public void saveSchedule(ScheduleDTO scheduleDTO);
    public List<String> findAvailableRooms(Integer theaterId, LocalDateTime startTime, LocalDateTime endTime);
    public boolean deleteSchedule(Integer id);
}
