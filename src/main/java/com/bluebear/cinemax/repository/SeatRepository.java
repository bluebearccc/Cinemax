package com.bluebear.cinemax.repository;

import com.bluebear.cinemax.entity.Seat;
import com.bluebear.cinemax.enumtype.Seat_Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SeatRepository extends JpaRepository<Seat, Integer> {
    List<Seat> findByStatus(Seat_Status status);
    List<Seat> findByRoom_RoomIDAndStatus(Integer roomID, Seat_Status status);
    List<Seat> findByRoom_RoomID(Integer roomID);
}
