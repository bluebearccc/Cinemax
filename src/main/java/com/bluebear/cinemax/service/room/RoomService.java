package com.bluebear.cinemax.service.room;

import com.bluebear.cinemax.dto.RoomDTO;
import com.bluebear.cinemax.entity.Room;

import java.util.List;

public interface RoomService {
    // --- CRUD ---
    RoomDTO createRoom(RoomDTO dto);

    RoomDTO updateRoom(Integer id, RoomDTO dto);

    void deleteRoom(Integer id);

    RoomDTO getRoomById(Integer id);

    List<RoomDTO> getAllRooms();

    List<RoomDTO> getRoomsByTheaterId(Integer theaterID);

    // --- Mapping ---
    RoomDTO toDTO(Room entity);

    Room toEntity(RoomDTO dto);
}
