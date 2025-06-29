package com.bluebear.cinemax.service.room;

import com.bluebear.cinemax.dto.RoomDTO;
import com.bluebear.cinemax.entity.Room;
import org.springframework.data.domain.Page;

import java.util.List;

public interface RoomService {
    RoomDTO createRoom(RoomDTO dto);

    RoomDTO updateRoom(Integer id, RoomDTO dto);

    void deleteRoom(Integer id);

    RoomDTO getRoomById(Integer id);

    Page<RoomDTO> getAllRooms();

    Page<RoomDTO> getRoomsByTheaterId(Integer theaterID);

    RoomDTO toDTO(Room entity);

    Room toEntity(RoomDTO dto);

    public List<RoomDTO> findAllRoomsByTheaterId(Integer theaterId);
}
