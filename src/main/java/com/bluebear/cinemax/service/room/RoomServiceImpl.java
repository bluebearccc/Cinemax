package com.bluebear.cinemax.service.room;

import com.bluebear.cinemax.dto.RoomDTO;
import com.bluebear.cinemax.dto.SeatDTO;
import com.bluebear.cinemax.entity.Room;
import com.bluebear.cinemax.entity.Seat;
import com.bluebear.cinemax.entity.Theater;
import com.bluebear.cinemax.enumtype.Room_Status;
import com.bluebear.cinemax.repository.RoomRepository;
import com.bluebear.cinemax.repository.ScheduleRepository;
import com.bluebear.cinemax.repository.SeatRepository;
import com.bluebear.cinemax.repository.TheaterRepository;
import com.bluebear.cinemax.service.schedule.ScheduleService;
import com.bluebear.cinemax.service.seat.SeatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class RoomServiceImpl implements RoomService {
    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private TheaterRepository theaterRepository;

    @Autowired
    private SeatRepository seatRepository;

    @Autowired
    private SeatService seatService;

    @Autowired
    private ScheduleService scheduleService;

    @Autowired
    private ScheduleRepository scheduleRepository;


    public RoomDTO createRoom(RoomDTO dto) {
        Room room = toEntity(dto);
        return toDTO(roomRepository.save(room));
    }


    public RoomDTO updateRoom(Integer id, RoomDTO dto) {
        Optional<Room> optionalRoom = roomRepository.findById(id);
        if (optionalRoom.isEmpty()) return null;

        Room room = optionalRoom.get();
        room.setName(dto.getName());
        room.setCollumn(dto.getCollumn());
        room.setRow(dto.getRow());
        room.setTypeOfRoom(dto.getTypeOfRoom());
        room.setStatus(dto.getStatus());
        if (dto.getSeats() != null) {room.setSeats(dto.getSeats().stream().map(seatDTO -> seatService.toEntity(seatDTO)).collect(Collectors.toList()));}
        if (dto.getSchedules() != null) {room.setSchedules(dto.getSchedules().stream().map(scheduleDTO -> scheduleService.toEntity(scheduleDTO)).collect(Collectors.toList()));}

        return toDTO(roomRepository.save(room));
    }


    public void deleteRoom(Integer id) {
        roomRepository.deleteById(id);
    }


    public RoomDTO getRoomById(Integer id) {
        return roomRepository.findById(id)
                .map(this::toDTO)
                .orElse(null);
    }

    public List<RoomDTO> getAllRooms() {
        return roomRepository.findByStatus(Room_Status.Active)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }


    public List<RoomDTO> getRoomsByTheaterId(Integer theaterID) {
        return roomRepository.findByTheater_TheaterIDAndStatus(theaterID, Room_Status.Active)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // ==================== Mapping ====================

    public RoomDTO toDTO(Room entity) {
        RoomDTO dto = new RoomDTO();
        dto.setRoomID(entity.getRoomID());
        dto.setTheaterID(entity.getTheater().getTheaterID());
        dto.setName(entity.getName());
        dto.setCollumn(entity.getCollumn());
        dto.setRow(entity.getRow());
        dto.setTypeOfRoom(entity.getTypeOfRoom());
        dto.setStatus(entity.getStatus());

        return dto;
    }

    public Room toEntity(RoomDTO dto) {
        Room entity = new Room();
        entity.setRoomID(dto.getRoomID());
        entity.setName(dto.getName());
        entity.setCollumn(dto.getCollumn());
        entity.setRow(dto.getRow());
        entity.setTypeOfRoom(dto.getTypeOfRoom());
        entity.setStatus(dto.getStatus());

        // Fetch Theater entity bằng theaterID
        Optional<Theater> optionalTheater = theaterRepository.findById(dto.getTheaterID());
        optionalTheater.ifPresent(entity::setTheater);

        if (dto.getSeats() != null) {entity.setSeats(dto.getSeats().stream().map(seatDTO -> seatService.toEntity(seatDTO)).collect(Collectors.toList()));}
        if (dto.getSchedules() != null) {entity.setSchedules(dto.getSchedules().stream().map(scheduleDTO -> scheduleService.toEntity(scheduleDTO)).collect(Collectors.toList()));}
        return entity;
    }
}
