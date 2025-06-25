package com.bluebear.cinemax.repository;

import com.bluebear.cinemax.entity.DetailSeat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface DetailSeatRepository extends JpaRepository<DetailSeat, Integer> {
    @Query("SELECT ds.seat.seatID FROM DetailSeat ds WHERE ds.schedule.scheduleID = :scheduleId")
    List<Integer> findBookedSeatIdsByScheduleId(Integer scheduleId);
}