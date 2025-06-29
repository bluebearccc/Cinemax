package com.bluebear.cinemax.repository;

import com.bluebear.cinemax.entity.Seat;
import com.bluebear.cinemax.enumtype.Seat_Status;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SeatRepository extends JpaRepository<Seat, Integer> {
    Page<Seat> findByStatus(Seat_Status status, Pageable pageable);
    Page<Seat> findByRoom_RoomIDAndStatus(Integer roomID, Seat_Status status, Pageable pageable);
}
