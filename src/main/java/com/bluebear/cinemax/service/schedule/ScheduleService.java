package com.bluebear.cinemax.service.schedule;

import com.bluebear.cinemax.dto.ScheduleDTO;
import com.bluebear.cinemax.entity.Schedule;

import java.util.Date;
import java.util.List;

public interface ScheduleService {
    // --- CRUD ---
    ScheduleDTO createSchedule(ScheduleDTO dto);

    ScheduleDTO updateSchedule(ScheduleDTO dto);

    void deleteSchedule(Integer scheduleID);

    ScheduleDTO getScheduleById(Integer scheduleID);

    List<ScheduleDTO> getAllSchedules();

    // --- Custom ---
    List<ScheduleDTO> getScheduleByMovieIdAndDate(Integer movieID, Date date);

    // --- Mapping ---
    ScheduleDTO toDTO(Schedule schedule);

    Schedule toEntity(ScheduleDTO dto);
}
