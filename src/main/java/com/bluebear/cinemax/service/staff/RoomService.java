package com.bluebear.cinemax.service.staff;

import com.bluebear.cinemax.dto.RoomDTO;
import com.bluebear.cinemax.entity.Room;

import java.util.List;

public interface RoomService {
    public List<RoomDTO> findAllRoomsByTheaterId(Integer theaterId);
    public RoomDTO getRoomById(Integer id);

    List<RoomDTO> findAllRooms();
}
