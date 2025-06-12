package com.bluebear.cinemax.service.staff;

import com.bluebear.cinemax.entity.Schedule;
import com.bluebear.cinemax.repository.staff.ScheduleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ScheduleServiceImpl implements ScheduleService{

    @Autowired
    ScheduleRepository scheduleRepository;

    @Override
    public List<Schedule> findByMovieId(Integer movieId) {
        return scheduleRepository.findByMovie_MovieId(movieId);
    }
}
