package com.bluebear.cinemax.repository.cashier;

import com.bluebear.cinemax.entity.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ScheduleRepository extends JpaRepository<Schedule, Integer> {
    // Tìm suất chiếu theo phim và ngày
    List<Schedule> findByMovie_MovieIdAndStartTimeBetween(Integer movieId, LocalDateTime startDate, LocalDateTime endDate);

    // Tìm suất chiếu theo rạp và ngày
    @Query("SELECT s FROM Schedule s WHERE s.room.theater.theaterId = :theaterId AND DATE(s.startTime) = :date AND s.status = :status")
    List<Schedule> findByTheaterAndDateAndStatus(@Param("theaterId") Integer theaterId, @Param("date") LocalDate date, @Param("status") Schedule.ScheduleStatus status);

    // Tìm suất chiếu active theo phòng
    List<Schedule> findByRoom_RoomIdAndStatusOrderByStartTime(Integer roomId, Schedule.ScheduleStatus status);

    // Tìm suất chiếu đang hoạt động
    List<Schedule> findByStatusAndStartTimeAfterOrderByStartTime(Schedule.ScheduleStatus status, LocalDateTime currentTime);
}
