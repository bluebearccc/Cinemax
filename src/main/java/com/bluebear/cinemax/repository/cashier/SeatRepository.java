package com.bluebear.cinemax.repository.cashier;

import com.bluebear.cinemax.entity.Seat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SeatRepository extends JpaRepository<Seat, Integer> {
    // Tìm ghế theo phòng
    List<Seat> findByRoom_RoomIdAndStatusOrderByPosition(Integer roomId, Seat.SeatStatus status);

    // Tìm ghế VIP theo phòng
    List<Seat> findByRoom_RoomIdAndIsVIPAndStatus(Integer roomId, Boolean isVIP, Seat.SeatStatus status);

    // Tìm ghế theo loại
    List<Seat> findByRoom_RoomIdAndSeatTypeAndStatus(Integer roomId, Seat.SeatType seatType, Seat.SeatStatus status);

    // Kiểm tra ghế còn trống cho suất chiếu
    @Query("SELECT s FROM Seat s WHERE s.room.roomId = :roomId AND s.status = :status AND s.seatId NOT IN " +
            "(SELECT ds.seat.seatId FROM DetailSeat ds WHERE ds.schedule.scheduleId = :scheduleId)")
    List<Seat> findAvailableSeatsForSchedule(@Param("roomId") Integer roomId, @Param("scheduleId") Integer scheduleId, @Param("status") Seat.SeatStatus status);
}