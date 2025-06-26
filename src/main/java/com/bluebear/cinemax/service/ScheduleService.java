package com.bluebear.cinemax.service;

import com.bluebear.cinemax.dto.ScheduleDTO;
import com.bluebear.cinemax.entity.Schedule;
import com.bluebear.cinemax.repository.ScheduleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ScheduleService {

    @Autowired
    private ScheduleRepository scheduleRepository;

    public Page<ScheduleDTO> getSchedulesByMovieIdAndDate(Integer theaterId, Integer movieId,
                                                          LocalDateTime startDate, LocalDateTime endDate,
                                                          Pageable pageable) {
        Page<Schedule> schedulesPage = scheduleRepository.findSchedulesByMovieIdAndTheaterIdAndDateRange(
                theaterId, movieId, startDate, endDate, pageable);

        List<ScheduleDTO> scheduleDTOs = schedulesPage.getContent().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return new PageImpl<>(scheduleDTOs, pageable, schedulesPage.getTotalElements());
    }

    private ScheduleDTO convertToDTO(Schedule schedule) {
        return ScheduleDTO.builder()
                .scheduleID(schedule.getScheduleID())
                .startTime(schedule.getStartTime())
                .endTime(schedule.getEndTime())
                .movieID(schedule.getMovie().getMovieID())
                .roomID(schedule.getRoom().getRoomID())
                .roomType(schedule.getRoom().getTypeOfRoom().name())
                .roomName(schedule.getRoom().getName())
                .status(schedule.getStatus())
                .build();
    }

    public ScheduleDTO getScheduleById(Integer scheduleId) {
        return scheduleRepository.findById(scheduleId)
                .map(this::convertToDTO)
                .orElse(null);
    }
}