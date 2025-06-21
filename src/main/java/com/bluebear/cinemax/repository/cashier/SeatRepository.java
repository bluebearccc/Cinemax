// Sửa đổi trong SeatRepository.java

package com.bluebear.cinemax.repository.cashier;

import com.bluebear.cinemax.entity.Room;
import com.bluebear.cinemax.entity.Seat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

// Thêm các method này vào SeatRepository

@Repository
public interface SeatRepository extends JpaRepository<Seat, Integer> {
    
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

    @Query("SELECT s FROM Seat s JOIN FETCH s.room WHERE s.room.roomId = :roomId AND s.status = :status ORDER BY s.position")
    List<Seat> findByRoomIdAndStatus(@Param("roomId") Integer roomId, @Param("status") Seat.SeatStatus status);

    @Query("""
            SELECT COUNT(*) AS AvailableSeats
            FROM Seat s
            JOIN Room r ON s.room.roomId = r.roomId
            JOIN Theater t ON r.theater.theaterId = t.theaterId
            WHERE t.theaterId = :theaterId
              AND r.roomId = :roomId
              AND s.seatId NOT IN (
                  SELECT ds.seat.seatId
                  FROM DetailSeat ds
                  WHERE ds.schedule.scheduleId = :scheduleId
              )
              AND s.status = 'Active'
            """)
    Long seatLeftInSchedule(@Param("theaterId") int theaterId,
                            @Param("roomId") int roomId,
                            @Param("scheduleId") int scheduleId);

}