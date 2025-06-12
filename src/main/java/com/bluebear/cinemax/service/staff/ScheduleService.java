package com.bluebear.cinemax.service.staff;

import com.bluebear.cinemax.entity.Schedule;

import java.util.List;

public interface ScheduleService {
    public List<Schedule> findByMovieId(Integer movieId);
}
