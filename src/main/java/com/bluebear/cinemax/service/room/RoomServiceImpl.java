package com.bluebear.cinemax.service.room;

import com.bluebear.cinemax.dto.RoomDTO;
import com.bluebear.cinemax.dto.ScheduleDTO;
import com.bluebear.cinemax.dto.SeatDTO;
import com.bluebear.cinemax.entity.Room;
import com.bluebear.cinemax.entity.Schedule;
import com.bluebear.cinemax.entity.Seat;
import com.bluebear.cinemax.entity.Theater;
import com.bluebear.cinemax.enumtype.*;
import com.bluebear.cinemax.repository.*;
import com.bluebear.cinemax.service.schedule.ScheduleService;
import com.bluebear.cinemax.service.seat.SeatService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class RoomServiceImpl implements RoomService {
    @Autowired
    private DetailSeatRepository detailSeatRepository;

    @Autowired
    private SeatRepository seatRepository;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private TheaterRepository theaterRepository;

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

    public Page<RoomDTO> getAllRooms() {
        return roomRepository.findByStatus(Room_Status.Active, Pageable.unpaged()).map(this::toDTO);
    }

    public Page<RoomDTO> getRoomsByTheaterId(Integer theaterID) {
        return roomRepository.findByTheater_TheaterIDAndStatus(theaterID, Room_Status.Active, Pageable.unpaged()).map(this::toDTO);
    }

    public RoomDTO toDTO(Room entity) {
        RoomDTO dto = new RoomDTO();
        dto.setRoomID(entity.getRoomID());
        dto.setTheaterID(entity.getTheater().getTheaterID());
        dto.setName(entity.getName());
        dto.setCollumn(entity.getCollumn());
        dto.setRow(entity.getRow());
        dto.setTypeOfRoom(entity.getTypeOfRoom());
        dto.setStatus(entity.getStatus());
        if (entity.getSeats() != null) {
            dto.setSeats(entity.getSeats().stream()
                    .map(this::convertSeatToDTO)
                    .collect(Collectors.toList()));
        }
        if (entity.getSchedules() != null) {
            dto.setSchedules(entity.getSchedules().stream()
                    .map(this::convertScheduleToDTO)
                    .collect(Collectors.toList()));
        }
        return dto;
    }
    private SeatDTO convertSeatToDTO(Seat seat) {
        if (seat == null) {
            return null;
        }
        return SeatDTO.builder()
                .seatID(seat.getSeatID())
                .roomID(seat.getRoom() != null ? seat.getRoom().getRoomID() : null)
                .seatType(seat.getSeatType())
                .position(seat.getPosition())
                .name(seat.getName())
                .isVIP(seat.getIsVIP())
                .unitPrice(seat.getUnitPrice())
                .status(seat.getStatus())
                .build();
    }
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
    public Room toEntity(RoomDTO dto) {
        Room entity = new Room();
        entity.setRoomID(dto.getRoomID());
        entity.setName(dto.getName());
        entity.setCollumn(dto.getCollumn());
        entity.setRow(dto.getRow());
        entity.setTypeOfRoom(dto.getTypeOfRoom());
        entity.setStatus(dto.getStatus());
        Optional<Theater> optionalTheater = theaterRepository.findById(dto.getTheaterID());
        optionalTheater.ifPresent(entity::setTheater);

        if (dto.getSeats() != null) {entity.setSeats(dto.getSeats().stream().map(seatDTO -> seatService.toEntity(seatDTO)).collect(Collectors.toList()));}
        if (dto.getSchedules() != null) {entity.setSchedules(dto.getSchedules().stream().map(scheduleDTO -> scheduleService.toEntity(scheduleDTO)).collect(Collectors.toList()));}
        return entity;
    }

    @Override
    public List<RoomDTO> findAllRoomsByTheaterId(Integer theaterId) {
        List<Room> rooms = roomRepository.findAllByTheater_TheaterID(theaterId);
        return rooms.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
    @Transactional
    @Override
    public RoomDTO addRoom(RoomDTO roomDTO) throws Exception {
        Optional<Room> latestRoomOptional = roomRepository
                .findTopByTheater_TheaterIDAndNameStartsWithOrderByNameDesc(roomDTO.getTheaterID(), "R");

        int maxRoomNumber = 0;
        if (latestRoomOptional.isPresent()) {
            String latestRoomName = latestRoomOptional.get().getName();
            // Lấy phần số từ tên phòng (ví dụ: "R06" -> 6)
            maxRoomNumber = Integer.parseInt(latestRoomName.substring(1));
        }

        // 2. Tạo tên phòng mới
        String newRoomName = String.format("R%02d", maxRoomNumber + 1);
        roomDTO.setName(newRoomName);

        // 3. Phần còn lại của phương thức giữ nguyên
        Theater theater = theaterRepository.findById(roomDTO.getTheaterID())
                .orElseThrow(() -> new Exception("Theater not found with id: " + roomDTO.getTheaterID()));
        Room room = toEntity(roomDTO);
        room.setTheater(theater);
        Room savedRoom = roomRepository.save(room);

        // --- Phần tạo ghế giữ nguyên ---
        // ...
        List<Seat> seatsToCreate = new ArrayList<>();
        int rows = savedRoom.getRow();
        int columns = savedRoom.getCollumn();
        TypeOfSeat seatType = TypeOfSeat.valueOf(savedRoom.getTypeOfRoom().name());
        for (int i = 0; i < rows; i++) {
            char rowChar = (char) ('A' + i);
            for (int j = 1; j <= columns; j++) {
                Seat newSeat = new Seat();
                newSeat.setRoom(savedRoom);
                newSeat.setSeatType(seatType);
                newSeat.setPosition(rowChar + String.valueOf(j));
                newSeat.setName(rowChar + String.valueOf(j));
                newSeat.setIsVIP(false);
                newSeat.setUnitPrice(0.0);
                newSeat.setStatus(Seat_Status.Active);
                seatsToCreate.add(newSeat);
            }
        }
        seatRepository.saveAll(seatsToCreate);

        return toDTO(savedRoom);
    }
    @Override
    public Integer findTheaterIdByRoomId(Integer roomId) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Room not found with ID: " + roomId));
        return room.getTheater().getTheaterID();
    }
    @Override
    @Transactional
    public void deleteRoomById(Integer roomId) throws Exception {
        Room roomToDelete = roomRepository.findById(roomId)
                .orElseThrow(() -> new Exception("Room with ID " + roomId + " not found."));

        LocalDateTime today = LocalDate.now().atStartOfDay();
        List<Schedule> futureSchedules = scheduleRepository.findConflictingSchedulesFromDate(
                roomId, Schedule_Status.Active, today);

        if (!futureSchedules.isEmpty()) {
            // (Logic tạo thông báo lỗi cho lịch chiếu tương lai giữ nguyên...)
            throw new Exception("Cannot delete this room. It has active future schedules.");
        }

        // === BƯỚC 2: KIỂM TRA LỊCH CHIẾU TRONG QUÁ KHỨ ĐÃ BÁN VÉ ===
        List<Schedule> allSchedulesForRoom = scheduleRepository.findByRoom_RoomID(roomId);
        StringBuilder pastConflictsDetails = new StringBuilder();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        for (Schedule pastSchedule : allSchedulesForRoom) {
            long ticketCount = detailSeatRepository.countBySchedule_ScheduleID(pastSchedule.getScheduleID());
            if (ticketCount > 0) {
                String movieName = (pastSchedule.getMovie() != null) ? pastSchedule.getMovie().getMovieName() : "N/A";
                String formattedDate = pastSchedule.getStartTime().format(formatter);
                pastConflictsDetails.append(String.format("\n- Movie '%s' in %s (had sold %d tickets)",
                        movieName, formattedDate, ticketCount));
            }
        }

        if (pastConflictsDetails.length() > 0) {
            String errorMessage = String.format(
                    "Cannot delete this room '%s'. It has appear in sold ticket:%s",
                    roomToDelete.getName(),
                    pastConflictsDetails.toString()
            );
            throw new Exception(errorMessage);
        }
        if (allSchedulesForRoom != null && !allSchedulesForRoom.isEmpty()) {
            scheduleRepository.deleteAll(allSchedulesForRoom);
        }

        List<Seat> seatsToDelete = seatRepository.findByRoom_RoomID(roomId);
        if (seatsToDelete != null && !seatsToDelete.isEmpty()) {
            seatRepository.deleteAll(seatsToDelete);
        }

        roomRepository.delete(roomToDelete);
    }
    @Override
    @Transactional
    public RoomDTO updateRoom(RoomDTO roomDTO) throws Exception {
        LocalDateTime today = LocalDate.now().atStartOfDay();
        List<Schedule> conflictingSchedules = scheduleRepository.findConflictingSchedulesFromDate(
                roomDTO.getRoomID(), Schedule_Status.Active, today);

        if (!conflictingSchedules.isEmpty()) {
            StringBuilder conflictDetails = new StringBuilder();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy 'time' HH:mm");

            for (Schedule conflict : conflictingSchedules) {
                String movieName = (conflict.getMovie() != null) ? conflict.getMovie().getMovieName() : "N/A";
                String formattedDateTime = conflict.getStartTime().format(formatter);

                conflictDetails.append(String.format("\n- Movie '%s' at %s", movieName, formattedDateTime));
            }

            String errorMessage = String.format(
                    "Cannot delete this room '%s'. This room already has active schedules in the future :%s",
                    roomDTO.getName(),
                    conflictDetails.toString()
            );
            throw new Exception(errorMessage);
        }

        Room existingRoom = roomRepository.findById(roomDTO.getRoomID())
                .orElseThrow(() -> new Exception("Room not found with ID: " + roomDTO.getRoomID()));
        TypeOfRoom oldType = existingRoom.getTypeOfRoom();
        TypeOfRoom newType = roomDTO.getTypeOfRoom();
        Map<String, Seat> existingSeatMap = new HashMap<>();
        List<Seat> existingSeats = seatRepository.findByRoom_RoomID(roomDTO.getRoomID());
        if (existingSeats != null) {
            for (Seat seat : existingSeats) {
                existingSeatMap.put(seat.getPosition(), seat);
            }
        }
        List<Seat> seatsToDelete = new ArrayList<>();
        List<Seat> seatsToCreate = new ArrayList<>();

        int newRows = roomDTO.getRow();
        int newCols = roomDTO.getCollumn();

        for (Seat existingSeat : existingSeatMap.values()) {
            char rowChar = existingSeat.getPosition().charAt(0);
            int seatRow = rowChar - 'A' + 1;
            int seatCol = Integer.parseInt(existingSeat.getPosition().substring(1));

            if (seatRow > newRows || seatCol > newCols) {
                seatsToDelete.add(existingSeat);
            }
        }

        for (int i = 0; i < newRows; i++) {
            char rowChar = (char) ('A' + i);
            for (int j = 1; j <= newCols; j++) {
                String currentPosition = rowChar + String.valueOf(j);
                if (!existingSeatMap.containsKey(currentPosition)) {
                    Seat newSeat = new Seat();
                    newSeat.setRoom(existingRoom);
                    newSeat.setSeatType(TypeOfSeat.valueOf(roomDTO.getTypeOfRoom().name()));
                    newSeat.setPosition(currentPosition);
                    newSeat.setName(currentPosition);
                    newSeat.setIsVIP(false);
                    newSeat.setUnitPrice(0.0);
                    newSeat.setStatus(Seat_Status.Active);
                    seatsToCreate.add(newSeat);
                }
            }
        }
        if (newType != oldType) {
            List<Seat> seatsToUpdate = seatRepository.findByRoom_RoomID(roomDTO.getRoomID());

            if (seatsToUpdate != null && !seatsToUpdate.isEmpty()) {
                TypeOfSeat newSeatType = (newType == TypeOfRoom.Couple) ? TypeOfSeat.Couple : TypeOfSeat.Single;
                for (Seat seat : seatsToUpdate) {
                    seat.setSeatType(newSeatType);
                }
                seatRepository.saveAll(seatsToUpdate);
            }
        }
        if (!seatsToDelete.isEmpty()) {
            List<Integer> seatIdsToDelete = seatsToDelete.stream()
                    .map(Seat::getSeatID)
                    .collect(Collectors.toList());

            long soldSeatCount = detailSeatRepository.countBySeat_SeatIDIn(seatIdsToDelete);

            // Nếu có vé đã bán, ném ra lỗi
            if (soldSeatCount > 0) {
                throw new Exception(String.format(
                        "Cannot reduce room size. At least %d seats that would be removed have already been sold in past schedules.",
                        soldSeatCount
                ));
            }
        }
        if (!seatsToDelete.isEmpty()) {
            seatRepository.deleteAllInBatch(seatsToDelete);
        }
        if (!seatsToCreate.isEmpty()) {
            seatRepository.saveAll(seatsToCreate);
        }

        existingRoom.setName(roomDTO.getName());
        existingRoom.setTypeOfRoom(roomDTO.getTypeOfRoom());
        existingRoom.setStatus(roomDTO.getStatus());
        existingRoom.setRow(newRows);
        existingRoom.setCollumn(newCols);

        Room finalUpdatedRoom = roomRepository.save(existingRoom);

        return toDTO(finalUpdatedRoom);
    }
}
