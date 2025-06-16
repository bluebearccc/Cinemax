package com.bluebear.cinemax.service.staff;

import com.bluebear.cinemax.entity.Schedule;
import com.bluebear.cinemax.repository.staff.ScheduleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Service
public class ScheduleServiceImpl implements ScheduleService{

    @Autowired
    ScheduleRepository scheduleRepository;

    @Override
    public List<Schedule> findByMovieId(Integer movieId) {
        return scheduleRepository.findByMovie_MovieId(movieId);
    }

    @Override
    public List<Schedule> findAllScheduleByMovieIdAndRoomIdAndDate(Integer movieId, Integer roomId, LocalDate date) {
        return scheduleRepository.findAllByMovieIdAndRoomId(movieId, roomId, date);
    }

    @Override
    public List<Schedule> findOverlappingSchedules(Integer roomId, LocalDateTime newStartTime, LocalDateTime newEndTime) {
        return scheduleRepository.findOverlappingSchedules(roomId, newStartTime, newEndTime);
    }

    @Override
    public void saveSchedule(Schedule schedule) {
        scheduleRepository.save(schedule);
    }


}
