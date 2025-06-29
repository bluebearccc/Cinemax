package com.bluebear.cinemax.repository;

import com.bluebear.cinemax.entity.Seat;
import com.bluebear.cinemax.enumtype.Seat_Status;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SeatRepository extends JpaRepository<Seat, Integer> {
    Page<Seat> findByStatus(Seat_Status status, Pageable pageable);
    Page<Seat> findByRoom_RoomIDAndStatus(Integer roomID, Seat_Status status, Pageable pageable);
    List<Seat> findByStatus(Seat_Status status);
    List<Seat> findByRoom_RoomIDAndStatus(Integer roomID, Seat_Status status);

    long countByRoom_RoomIDAndStatus(Integer roomId, Seat_Status seatStatus);

    @Query("SELECT COUNT(*) FROM Seat s WHERE s.room.roomID = :roomId AND s.isVIP = :isVIP AND s.status = :status")
    long countByRoom_RoomIDAndIsVIPAndStatus(Integer roomId, boolean b, Seat_Status seatStatus);

    @Query("SELECT s FROM Seat s WHERE s.room.roomID = :roomId")
    List<Seat> findByRoom_RoomID(Integer roomId);
}
