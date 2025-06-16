package com.bluebear.cinemax.service.staff;

import com.bluebear.cinemax.entity.Room;

import java.util.List;

public interface RoomService {
    public List<Room> findAllRoomsByTheaterId(Integer theaterId);
    public Room getRoomById(Integer id);
}
