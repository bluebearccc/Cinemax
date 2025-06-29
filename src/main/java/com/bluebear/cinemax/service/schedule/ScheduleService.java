package com.bluebear.cinemax.service.schedule;

import com.bluebear.cinemax.dto.ScheduleDTO;
import com.bluebear.cinemax.entity.Schedule;
import org.springframework.data.domain.Page;

import java.time.LocalDateTime;

public interface ScheduleService {
    ScheduleDTO createSchedule(ScheduleDTO dto);

    ScheduleDTO updateSchedule(ScheduleDTO dto);

    void deleteSchedule(Integer scheduleID);

    ScheduleDTO getScheduleById(Integer scheduleID);

    Page<ScheduleDTO> getScheduleByMovieIdAndDate(Integer movieID, LocalDateTime date);

    Page<ScheduleDTO> getScheduleByMovieIdAndTheaterIdAndDateAndRoomType(Integer movieID, Integer theaterID, LocalDateTime date, String roomType);

    Page<ScheduleDTO> getAllSchedules();

    void calculateNumOfSeatLeft(ScheduleDTO scheduleDTO);

    ScheduleDTO toDTO(Schedule schedule);

    Schedule toEntity(ScheduleDTO dto);
}
