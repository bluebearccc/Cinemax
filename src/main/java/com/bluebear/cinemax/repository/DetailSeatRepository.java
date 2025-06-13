package com.bluebear.cinemax.repository;

import com.bluebear.cinemax.entity.DetailSeat;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface DetailSeatRepository extends JpaRepository<DetailSeat, Integer> {
    List<DetailSeat> findByScheduleScheduleId(Integer scheduleId);
    boolean existsBySeatSeatIdAndScheduleScheduleId(Integer seatId, Integer scheduleId);
    @Modifying
    @Transactional
    @Query(value = "INSERT INTO Detail_Seat (invoiceId, seatId, status, scheduleId) VALUES (:invoiceId, :seatId, :status, :scheduleId)", nativeQuery = true)
    void insertDetailSeat(@Param("invoiceId") int invoiceId,
                          @Param("seatId") int seatId,
                          @Param("status") String status,
                          @Param("scheduleId") int scheduleId);
}
