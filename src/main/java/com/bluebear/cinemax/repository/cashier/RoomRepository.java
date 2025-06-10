package com.bluebear.cinemax.repository.cashier;

import com.bluebear.cinemax.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoomRepository extends JpaRepository<Room, Integer> {
    // Tìm phòng theo rạp
    List<Room> findByTheater_TheaterIdAndStatusOrderByName(Integer theaterId, Room.RoomStatus status);

    // Tìm phòng theo loại
    List<Room> findByTheater_TheaterIdAndTypeOfRoomAndStatus(Integer theaterId, Room.RoomType roomType, Room.RoomStatus status);
}
