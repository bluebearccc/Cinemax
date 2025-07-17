package com.bluebear.cinemax.service.room;

import com.bluebear.cinemax.dto.RoomDTO;
import com.bluebear.cinemax.entity.Room;
import com.bluebear.cinemax.entity.Seat;
import com.bluebear.cinemax.entity.Theater;
import com.bluebear.cinemax.enumtype.Seat_Status;
import com.bluebear.cinemax.enumtype.TypeOfSeat;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
    public RoomDTO addRoom(RoomDTO roomDTO) throws Exception;
    public Integer findTheaterIdByRoomId(Integer roomId);
    public void deleteRoomById(Integer roomId) throws Exception;
    public RoomDTO updateRoom(RoomDTO roomDTO) throws Exception;
}
