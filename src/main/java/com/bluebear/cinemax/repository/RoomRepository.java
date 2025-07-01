package com.bluebear.cinemax.repository;

import com.bluebear.cinemax.entity.DetailSeat;
import com.bluebear.cinemax.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoomRepository extends JpaRepository<Room, Integer> {
}
