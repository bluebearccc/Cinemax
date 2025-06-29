package com.bluebear.cinemax.service.schedule;

import com.bluebear.cinemax.dto.ScheduleDTO;
import com.bluebear.cinemax.entity.Schedule;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface ScheduleService {
    ScheduleDTO createSchedule(ScheduleDTO dto);

    ScheduleDTO updateSchedule(ScheduleDTO dto);

    boolean deleteSchedule(Integer scheduleID);

    ScheduleDTO getScheduleById(Integer scheduleID);

    Page<ScheduleDTO> getScheduleByMovieIdAndDate(Integer movieID, LocalDateTime date);

    Page<ScheduleDTO> getScheduleByMovieIdAndTheaterIdAndDateAndRoomType(Integer movieID, Integer theaterID, LocalDateTime date, String roomType);

    Page<ScheduleDTO> getAllSchedules();

    void calculateNumOfSeatLeft(ScheduleDTO scheduleDTO);

    ScheduleDTO toDTO(Schedule schedule);

    Schedule toEntity(ScheduleDTO dto);

    public List<ScheduleDTO> findByMovieId(Integer movieId);

    public List<ScheduleDTO> findAllScheduleByMovieIdAndRoomIdAndDate(Integer movieId, Integer roomId, LocalDate date);

    public void saveSchedule(ScheduleDTO scheduleDTO);

    public List<String> findAvailableRooms(Integer theaterId, LocalDateTime startTime, LocalDateTime endTime);

    public boolean isExisted(Integer scheduleId);

    ScheduleDTO isRoomAvailableForUpdate(Integer roomId, LocalDateTime startTime, LocalDateTime endTime, Integer scheduleId);

    Page<ScheduleDTO> getSchedulesByMovieIdAndDate(Integer theaterId, Integer movieId,
                                                   LocalDateTime startDate, LocalDateTime endDate,
                                                   Pageable pageable);
}
