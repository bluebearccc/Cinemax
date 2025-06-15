// Sửa đổi trong SeatRepository.java

package com.bluebear.cinemax.repository.cashier;

import com.bluebear.cinemax.entity.Seat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

// Thêm các method này vào SeatRepository

@Repository
public interface SeatRepository extends JpaRepository<Seat, Integer> {

    // Method hiện tại đang được sử dụng nhưng có thể chưa được define
    @Query("SELECT DISTINCT s FROM Seat s " +
            "LEFT JOIN FETCH s.room r " +
            "WHERE s.room.roomId IN (" +
            "    SELECT sch.room.roomId FROM Schedule sch WHERE sch.scheduleId = :scheduleId" +
            ") AND s.status = :status " +
            "ORDER BY s.position")
    List<Seat> findSeatsWithBookingsByScheduleIdAndStatus(
            @Param("scheduleId") Integer scheduleId,
            @Param("status") Seat.SeatStatus status
    );

    // Alternative method - lấy ghế theo room của schedule
    @Query("SELECT s FROM Seat s " +
            "JOIN FETCH s.room r " +
            "WHERE r.roomId = (" +
            "    SELECT sch.room.roomId FROM Schedule sch WHERE sch.scheduleId = :scheduleId" +
            ") AND s.status = :status " +
            "ORDER BY s.position")
    List<Seat> findSeatsByScheduleIdAndStatus(
            @Param("scheduleId") Integer scheduleId,
            @Param("status") Seat.SeatStatus status
    );

    // Method để lấy ghế theo roomId
    @Query("SELECT s FROM Seat s JOIN FETCH s.room WHERE s.room.roomId = :roomId AND s.status = :status ORDER BY s.position")
    List<Seat> findByRoomIdAndStatus(@Param("roomId") Integer roomId, @Param("status") Seat.SeatStatus status);
}