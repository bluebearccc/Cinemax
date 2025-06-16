package com.bluebear.cinemax.service.staff;

import com.bluebear.cinemax.entity.Room;
import com.bluebear.cinemax.repository.staff.RoomRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RoomServiceImpl implements RoomService{

    @Autowired
    private RoomRepository roomRepository;
    @Override
    public List<Room> findAllRoomsByTheaterId(Integer theaterId) {
        return roomRepository.findAllByTheater_TheaterId(theaterId);
    }

    @Override
    public Room getRoomById(Integer id) {
        return roomRepository.findById(id).orElse(null);
    }
}
