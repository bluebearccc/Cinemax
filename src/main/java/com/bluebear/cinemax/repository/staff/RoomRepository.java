package com.bluebear.cinemax.repository.staff;

import com.bluebear.cinemax.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoomRepository extends JpaRepository<Room, Integer> {
    List<Room> findAllByTheater_TheaterId(Integer theaterTheaterId);
}
