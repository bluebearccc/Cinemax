package com.bluebear.cinemax.repository;

import com.bluebear.cinemax.entity.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ScheduleRepository extends JpaRepository<Schedule, Integer> {
}
