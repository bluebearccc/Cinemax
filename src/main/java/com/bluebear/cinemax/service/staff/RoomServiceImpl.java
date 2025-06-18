package com.bluebear.cinemax.service.staff;

import com.bluebear.cinemax.dto.RoomDTO;
import com.bluebear.cinemax.dto.SeatDTO;
import com.bluebear.cinemax.dto.ScheduleDTO;
import com.bluebear.cinemax.entity.Room;
import com.bluebear.cinemax.entity.Seat;
import com.bluebear.cinemax.entity.Schedule;
import com.bluebear.cinemax.entity.Theater; // Assuming you have a Theater entity
import com.bluebear.cinemax.repository.RoomRepository; // Assuming this is your Room entity repository
import com.bluebear.cinemax.repository.TheaterRepository; // Assuming you have a TheaterRepository
import com.bluebear.cinemax.repository.SeatRepository; // Assuming you have a SeatRepository
import com.bluebear.cinemax.repository.ScheduleRepository; // Assuming you have a ScheduleRepository
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class RoomServiceImpl implements RoomService {

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private TheaterRepository theaterRepository; // Inject TheaterRepository

    @Autowired
    private SeatRepository seatRepository; // Inject SeatRepository if you plan to handle seats here

    @Autowired
    private ScheduleRepository scheduleRepository; // Inject ScheduleRepository if you plan to handle schedules here


    // Helper method to convert Room entity to RoomDTO
    private RoomDTO convertToDTO(Room room) {
        if (room == null) {
            return null;
        }
        RoomDTO roomDTO = RoomDTO.builder()
                .roomID(room.getRoomID())
                .name(room.getName())
                .column(room.getColumn()) // Assuming 'column' in RoomDTO maps to 'roomColumn' in Room entity
                .row(room.getRow())       // Assuming 'row' in RoomDTO maps to 'roomRow' in Room entity
                .typeOfRoom(room.getTypeOfRoom())
                .status(room.getStatus())
                .theaterID(room.getTheater() != null ? room.getTheater().getTheaterID() : null)
                .build();

        // If you need to include seats and schedules in the RoomDTO when fetching rooms
        if (room.getSeats() != null) {
            roomDTO.setSeats(room.getSeats().stream()
                    .map(this::convertSeatToDTO)
                    .collect(Collectors.toList()));
        }
        if (room.getSchedules() != null) {
            roomDTO.setSchedules(room.getSchedules().stream()
                    .map(this::convertScheduleToDTO)
                    .collect(Collectors.toList()));
        }
        return roomDTO;
    }

    // Helper method to convert RoomDTO to Room entity
    private Room convertToEntity(RoomDTO roomDTO) {
        if (roomDTO == null) {
            return null;
        }

        Room room = new Room();
        room.setRoomID(roomDTO.getRoomID());
        room.setName(roomDTO.getName());
        room.setColumn(roomDTO.getColumn()); // Mapping DTO 'column' to entity 'roomColumn'
        room.setRow(roomDTO.getRow());       // Mapping DTO 'row' to entity 'roomRow'
        room.setTypeOfRoom(roomDTO.getTypeOfRoom());
        room.setStatus(roomDTO.getStatus());

        if (roomDTO.getTheaterID() != null) {
            theaterRepository.findById(roomDTO.getTheaterID())
                    .ifPresent(room::setTheater);
        }

        // You might need to handle saving/updating nested seats and schedules separately
        // or fetch them from the database if they already exist.
        // For simplicity, we're not converting nested DTOs back to entities here,
        // as they are typically managed by their own services.
        return room;
    }

    // Helper method to convert Seat entity to SeatDTO (assuming SeatDTO and Seat entity exist)
    private SeatDTO convertSeatToDTO(Seat seat) {
        if (seat == null) {
            return null;
        }
        return SeatDTO.builder()
                .seatID(seat.getSeatID())
                .roomID(seat.getRoom() != null ? seat.getRoom().getRoomID() : null)
                .seatType(seat.getSeatType())
                .position(seat.getPosition())
                .isVIP(seat.isVIP())
                .unitPrice(seat.getUnitPrice())
                .status(seat.getStatus())
                .build();
    }

    // Helper method to convert Schedule entity to ScheduleDTO (assuming ScheduleDTO and Schedule entity exist)
    private ScheduleDTO convertScheduleToDTO(Schedule schedule) {
        if (schedule == null) {
            return null;
        }
        return ScheduleDTO.builder()
                .scheduleID(schedule.getScheduleID())
                .startTime(schedule.getStartTime())
                .endTime(schedule.getEndTime())
                .movieID(schedule.getMovie() != null ? schedule.getMovie().getMovieID() : null)
                .roomID(schedule.getRoom() != null ? schedule.getRoom().getRoomID() : null)
                .status(schedule.getStatus())
                .build();
    }

    @Override
    public List<RoomDTO> findAllRoomsByTheaterId(Integer theaterId) {
        List<Room> rooms = roomRepository.findAllByTheater_TheaterID(theaterId);
        return rooms.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public RoomDTO getRoomById(Integer id) {
        Optional<Room> roomOptional = roomRepository.findById(id);
        return roomOptional.map(this::convertToDTO).orElse(null);
    }


}