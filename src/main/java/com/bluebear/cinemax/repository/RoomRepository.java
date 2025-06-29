package com.bluebear.cinemax.repository;

import com.bluebear.cinemax.entity.Room;
import com.bluebear.cinemax.enumtype.Room_Status;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoomRepository extends JpaRepository<Room, Integer> {
    Page<Room> findByStatus(Room_Status status, Pageable pageable);
    Page<Room> findByTheater_TheaterIDAndStatus(Integer theaterID, Room_Status status, Pageable pageable);
}
