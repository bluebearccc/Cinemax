package com.bluebear.cinemax.repository;

import com.bluebear.cinemax.entity.DetailSeat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface DetailSeatRepository extends JpaRepository<DetailSeat, Integer> {
    @Query("SELECT ds.seat.seatID FROM DetailSeat ds WHERE ds.schedule.scheduleID = :scheduleId")
    List<Integer> findBookedSeatIdsByScheduleId(Integer scheduleId);
    int countBySchedule_ScheduleID(Integer scheduleId);
    int countBySeat_SeatIDIn(List<Integer> seatIds);
    @Query("SELECT ds FROM DetailSeat ds JOIN ds.schedule s WHERE ds.id = :seatId AND s.startTime > :currentTime")
    List<DetailSeat> findFutureBookingsBySeatId(@Param("seatId") Integer seatId, @Param("currentTime") LocalDateTime currentTime);
    long countBySeat_SeatID(Integer seatId);
}