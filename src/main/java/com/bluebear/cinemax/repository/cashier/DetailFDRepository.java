package com.bluebear.cinemax.repository.cashier;

import com.bluebear.cinemax.entity.DetailFD;
import com.bluebear.cinemax.entity.DetailSeat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DetailFDRepository extends JpaRepository<DetailFD, Integer> {
    @Query("SELECT ds.seat.seatId FROM DetailSeat ds WHERE ds.schedule.scheduleId = :scheduleId")
    List<Integer> findBookedSeatIdsByScheduleId(@Param("scheduleId") Integer scheduleId);

    @Query("SELECT ds FROM DetailSeat ds " +
            "JOIN FETCH ds.seat " +
            "JOIN FETCH ds.invoice " +
            "WHERE ds.schedule.scheduleId = :scheduleId")
    List<DetailSeat> findByScheduleId(@Param("scheduleId") Integer scheduleId);

    @Query("SELECT ds FROM DetailSeat ds " +
            "JOIN FETCH ds.seat " +
            "JOIN FETCH ds.invoice " +
            "WHERE ds.schedule.scheduleId = :scheduleId AND ds.seat.seatId = :seatId")
    Optional<DetailSeat> findByScheduleIdAndSeatId(
            @Param("scheduleId") Integer scheduleId,
            @Param("seatId") Integer seatId
    );
}