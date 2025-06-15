package com.bluebear.cinemax.service.theater;

import com.bluebear.cinemax.dto.RoomDTO;
import com.bluebear.cinemax.dto.TheaterDTO;
import com.bluebear.cinemax.entity.Room;
import com.bluebear.cinemax.entity.Theater;
import com.bluebear.cinemax.enumtype.Theater_Status;
import com.bluebear.cinemax.repository.RoomRepository;
import com.bluebear.cinemax.repository.TheaterRepository;
import com.bluebear.cinemax.service.room.RoomService;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class TheaterServiceImpl implements TheaterService {
    @Autowired
    private TheaterRepository theaterRepository;
    @Autowired
    private RoomRepository roomRepository;
    @Autowired
    private RoomService roomService;

    public TheaterDTO createTheater(TheaterDTO dto) {
        Theater theater = toEntity(dto);
        return toDTO(theaterRepository.save(theater));
    }

    public TheaterDTO updateTheater(Integer id, TheaterDTO dto) {
        Optional<Theater> optionalTheater = theaterRepository.findById(id);
        if (optionalTheater.isEmpty()) return null;

        Theater existing = optionalTheater.get();
        existing.setTheaterName(dto.getTheaterName());
        existing.setAddress(dto.getAddress());
        existing.setImage(dto.getImage());
        existing.setRoomQuantity(dto.getRoomQuantity());
        existing.setStatus(dto.getStatus());
        if (dto.getRooms() != null) {
            existing.setRooms(dto.getRooms().stream().map(roomDTO -> roomService.toEntity(roomDTO)).collect(Collectors.toList()));
        }

        return toDTO(theaterRepository.save(existing));
    }

    public void deleteTheater(Integer id) {
        theaterRepository.deleteById(id);
    }

    public TheaterDTO getTheaterById(Integer id) {
        return theaterRepository.findById(id)
                .map(this::toDTO)
                .orElse(null);
    }

    public List<TheaterDTO> getAllTheaters() {
        return theaterRepository.findByStatus(Theater_Status.Active).stream().map(this::toDTO).collect(Collectors.toList());
    }

    // ==================== Mapping ====================

    public TheaterDTO toDTO(Theater entity) {
        TheaterDTO dto = new TheaterDTO();
        dto.setTheaterID(entity.getTheaterID());
        dto.setTheaterName(entity.getTheaterName());
        dto.setAddress(entity.getAddress());
        dto.setImage(entity.getImage());
        dto.setRoomQuantity(entity.getRoomQuantity());
        dto.setStatus(entity.getStatus());
        return dto;
    }

    public Theater toEntity(TheaterDTO dto) {
        Theater entity = new Theater();
        entity.setTheaterID(dto.getTheaterID());
        entity.setTheaterName(dto.getTheaterName());
        entity.setAddress(dto.getAddress());
        entity.setImage(dto.getImage());
        entity.setRoomQuantity(dto.getRoomQuantity());
        entity.setStatus(dto.getStatus());
        if (dto.getRooms() != null) {entity.setRooms(dto.getRooms().stream().map(roomDTO -> roomService.toEntity(roomDTO)).collect(Collectors.toList()));}
        return entity;
    }
}
