package com.bluebear.cinemax.repository;

import com.bluebear.cinemax.entity.Room;
import com.bluebear.cinemax.enumtype.Room_Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoomRepository extends JpaRepository<Room, Integer> {
    List<Room> findByStatus(Room_Status status);
    List<Room> findByTheater_TheaterIDAndStatus(Integer theaterID, Room_Status status);
}
