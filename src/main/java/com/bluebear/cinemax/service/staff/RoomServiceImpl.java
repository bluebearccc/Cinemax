package com.bluebear.cinemax.service.staff;

import com.bluebear.cinemax.dto.RoomDTO;
import com.bluebear.cinemax.dto.SeatDTO;
import com.bluebear.cinemax.dto.ScheduleDTO;
import com.bluebear.cinemax.entity.Room;
import com.bluebear.cinemax.entity.Seat;
import com.bluebear.cinemax.entity.Schedule;
import com.bluebear.cinemax.entity.Theater; // Assuming you have a Theater entity
import com.bluebear.cinemax.enumtype.Seat_Status;
import com.bluebear.cinemax.enumtype.TypeOfSeat;
import com.bluebear.cinemax.repository.RoomRepository; // Assuming this is your Room entity repository
import com.bluebear.cinemax.repository.TheaterRepository; // Assuming you have a TheaterRepository
import com.bluebear.cinemax.repository.SeatRepository; // Assuming you have a SeatRepository
import com.bluebear.cinemax.repository.ScheduleRepository; // Assuming you have a ScheduleRepository
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
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
    private ScheduleRepository scheduleRepository;

    private RoomDTO convertToDTO(Room room) {
        if (room == null) {
            return null;
        }
        RoomDTO roomDTO = RoomDTO.builder()
                .roomID(room.getRoomID())
                .name(room.getName())
                .column(room.getColumn())
                .row(room.getRow())
                .typeOfRoom(room.getTypeOfRoom())
                .status(room.getStatus())
                .theaterID(room.getTheater() != null ? room.getTheater().getTheaterID() : null)
                .build();


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

    private Room convertToEntity(RoomDTO roomDTO) {
        if (roomDTO == null) {
            return null;
        }

        Room room = new Room();
        room.setRoomID(roomDTO.getRoomID());
        room.setName(roomDTO.getName());
        room.setColumn(roomDTO.getColumn());
        room.setRow(roomDTO.getRow());
        room.setTypeOfRoom(roomDTO.getTypeOfRoom());
        room.setStatus(roomDTO.getStatus());

        if (roomDTO.getTheaterID() != null) {
            theaterRepository.findById(roomDTO.getTheaterID())
                    .ifPresent(room::setTheater);
        }

        return room;
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
                .isVIP(seat.isVIP())
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

    @Override
    public List<RoomDTO> findAllRooms() {
        return List.of();
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
        Room room = convertToEntity(roomDTO);
        room.setTheater(theater);
        Room savedRoom = roomRepository.save(room);

        // --- Phần tạo ghế giữ nguyên ---
        // ...
        List<Seat> seatsToCreate = new ArrayList<>();
        int rows = savedRoom.getRow();
        int columns = savedRoom.getColumn();
        TypeOfSeat seatType = TypeOfSeat.valueOf(savedRoom.getTypeOfRoom().name());
        for (int i = 0; i < rows; i++) {
            char rowChar = (char) ('A' + i);
            for (int j = 1; j <= columns; j++) {
                Seat newSeat = new Seat();
                newSeat.setRoom(savedRoom);
                newSeat.setSeatType(seatType);
                newSeat.setPosition(rowChar + String.valueOf(j));
                newSeat.setName(rowChar + String.valueOf(j));
                newSeat.setVIP(false);
                newSeat.setUnitPrice(0.0);
                newSeat.setStatus(Seat_Status.Active);
                seatsToCreate.add(newSeat);
            }
        }
        seatRepository.saveAll(seatsToCreate);

        return convertToDTO(savedRoom);
    }
    @Override
    @Transactional
    public void deleteRoomById(Integer roomId) throws Exception {
        // Tìm phòng để lấy tên, đồng thời kiểm tra xem phòng có tồn tại không
        Room roomToDelete = roomRepository.findById(roomId)
                .orElseThrow(() -> new Exception("Room with ID " + roomId + " not found."));

        // B1: Tìm các lịch chiếu liên quan đến phòng này.
        List<Schedule> conflictingSchedules = scheduleRepository.findByRoom_RoomID(roomId);

        // B2: Nếu danh sách không rỗng, tức là có xung đột.
        if (!conflictingSchedules.isEmpty()) {
            // Lấy thông tin từ lịch chiếu đầu tiên tìm thấy để tạo thông báo lỗi.
            Schedule firstConflict = conflictingSchedules.get(0);

            // Lấy tên phim (đảm bảo movie không null)
            String movieName = (firstConflict.getMovie() != null) ? firstConflict.getMovie().getMovieName() : "N/A";

            // Lấy và định dạng ngày chiếu
            String formattedDate = firstConflict.getStartTime().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));

            // Tạo thông báo lỗi chi tiết và ném ra Exception.
            String errorMessage = String.format(
                    "Cannot delete room '%s'. It has a schedule for the movie '%s' on %s. Please remove it from all schedules first.",
                    roomToDelete.getName(),
                    movieName,
                    formattedDate
            );
            throw new Exception(errorMessage);
        }

        // B3: Nếu không có xung đột, tiến hành xóa tất cả các ghế thuộc phòng này.
        List<Seat> seatsToDelete = seatRepository.findByRoom_RoomID(roomId);
        if (seatsToDelete != null && !seatsToDelete.isEmpty()) {
            for (Seat seat : seatsToDelete) {
                seatRepository.delete(seat);
            }
        }

        // B4: Xóa phòng.
        roomRepository.delete(roomToDelete);
    }
    @Override
    public Integer findTheaterIdByRoomId(Integer roomId) {
        // 1. Dùng repository để tìm Room theo ID.
        //    Phương thức này trả về một Optional, có thể chứa Room hoặc rỗng.
        Room room = roomRepository.findById(roomId)
                // 2. Nếu không tìm thấy, ném ra một exception để báo lỗi.
                .orElseThrow(() -> new RuntimeException("Room not found with ID: " + roomId));

        // 3. Nếu tìm thấy, lấy ra đối tượng Theater từ Room và trả về ID của Theater đó.
        return room.getTheater().getTheaterID();
    }
}