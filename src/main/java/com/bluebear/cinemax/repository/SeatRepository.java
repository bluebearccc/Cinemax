package com.bluebear.cinemax.repository;

import com.bluebear.cinemax.entity.Seat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SeatRepository extends JpaRepository<Seat, Integer> {
    List<Seat> findByRoomRoomId(Integer roomId);
    @Query("SELECT ds.schedule.scheduleId FROM DetailSeat ds WHERE ds.seat.seatId = :seatId")
    List<Integer> findScheduleIdsBySeatId(@Param("seatId") Integer seatId);
}
