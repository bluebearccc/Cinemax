package com.bluebear.cinemax.repository.staff;

import com.bluebear.cinemax.entity.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ScheduleRepository extends JpaRepository<Schedule, Integer> {
    List<Schedule> findByMovie_MovieId(Integer movieId);
}
