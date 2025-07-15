package com.bluebear.cinemax.service.staff;

import com.bluebear.cinemax.dto.RoomDTO;
import com.bluebear.cinemax.dto.SeatDTO;
import com.bluebear.cinemax.dto.ScheduleDTO;
import com.bluebear.cinemax.entity.Room;
import com.bluebear.cinemax.entity.Seat;
import com.bluebear.cinemax.entity.Schedule;
import com.bluebear.cinemax.entity.Theater; // Assuming you have a Theater entity
import com.bluebear.cinemax.enumtype.Schedule_Status;
import com.bluebear.cinemax.enumtype.Seat_Status;
import com.bluebear.cinemax.enumtype.TypeOfRoom;
import com.bluebear.cinemax.enumtype.TypeOfSeat;
import com.bluebear.cinemax.repository.*;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;
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

    @Autowired
    private DetailSeatRepository detailSeatRepository;

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
        Room roomToDelete = roomRepository.findById(roomId)
                .orElseThrow(() -> new Exception("Room with ID " + roomId + " not found."));

        // === BƯỚC 1: KIỂM TRA LỊCH CHIẾU ACTIVE TRONG TƯƠNG LAI ===
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
            int ticketCount = detailSeatRepository.countBySchedule_ScheduleID(pastSchedule.getScheduleID());
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
    public Integer findTheaterIdByRoomId(Integer roomId) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Room not found with ID: " + roomId));
        return room.getTheater().getTheaterID();
    }

    @Override
    @Transactional
    public RoomDTO updateRoom(RoomDTO roomDTO) throws Exception {
        // === BƯỚC 1: VALIDATION (GIỮ NGUYÊN NHƯ CŨ) ===
        LocalDateTime today = LocalDate.now().atStartOfDay();
        List<Schedule> conflictingSchedules = scheduleRepository.findConflictingSchedulesFromDate(
                roomDTO.getRoomID(), Schedule_Status.Active, today);

        if (!conflictingSchedules.isEmpty()) {
            // Dùng StringBuilder để xây dựng chuỗi thông báo hiệu quả
            StringBuilder conflictDetails = new StringBuilder();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy 'time' HH:mm");

            // Dùng vòng lặp for để duyệt qua TẤT CẢ các lịch chiếu xung đột
            for (Schedule conflict : conflictingSchedules) {
                String movieName = (conflict.getMovie() != null) ? conflict.getMovie().getMovieName() : "N/A";
                String formattedDateTime = conflict.getStartTime().format(formatter);

                // Thêm chi tiết của mỗi lịch chiếu vào chuỗi
                conflictDetails.append(String.format("\n- Movie '%s' at %s", movieName, formattedDateTime));
            }

            // Tạo thông báo lỗi cuối cùng, bao gồm tất cả các chi tiết
            String errorMessage = String.format(
                    "Cannot delete this room '%s'. This room already has active schedules in the future :%s",
                    roomDTO.getName(),
                    conflictDetails.toString()
            );
            throw new Exception(errorMessage);
        }

        // === BƯỚC 2: LẤY TRẠNG THÁI HIỆN TẠI ===
        Room existingRoom = roomRepository.findById(roomDTO.getRoomID())
                .orElseThrow(() -> new Exception("Room not found with ID: " + roomDTO.getRoomID()));
        TypeOfRoom oldType = existingRoom.getTypeOfRoom();
        TypeOfRoom newType = roomDTO.getTypeOfRoom();
        // Đưa các ghế hiện có vào một Map để tra cứu nhanh bằng vị trí ("A1", "A2"...)
        Map<String, Seat> existingSeatMap = new HashMap<>();

// 2. Lấy danh sách tất cả các ghế từ database như bình thường
        List<Seat> existingSeats = seatRepository.findByRoom_RoomID(roomDTO.getRoomID());

// 3. Dùng vòng lặp for-each để duyệt qua từng ghế trong danh sách
        if (existingSeats != null) {
            for (Seat seat : existingSeats) {
                // Với mỗi ghế, thêm nó vào Map:
                // - Key: là vị trí của ghế (ví dụ: "A1", "C5")
                // - Value: là chính đối tượng ghế (seat) đó
                existingSeatMap.put(seat.getPosition(), seat);
            }
        }
        // === BƯỚC 3: TÍNH TOÁN SỰ KHÁC BIỆT ===
        List<Seat> seatsToDelete = new ArrayList<>();
        List<Seat> seatsToCreate = new ArrayList<>();

        int newRows = roomDTO.getRow();
        int newCols = roomDTO.getColumn();

        // 3a. Tìm các ghế cần XÓA
        for (Seat existingSeat : existingSeatMap.values()) {
            char rowChar = existingSeat.getPosition().charAt(0);
            int seatRow = rowChar - 'A' + 1;
            int seatCol = Integer.parseInt(existingSeat.getPosition().substring(1));

            if (seatRow > newRows || seatCol > newCols) {
                seatsToDelete.add(existingSeat);
            }
        }

        // 3b. Tìm các vị trí cần TẠO ghế mới
        for (int i = 0; i < newRows; i++) {
            char rowChar = (char) ('A' + i);
            for (int j = 1; j <= newCols; j++) {
                String currentPosition = rowChar + String.valueOf(j);
                // Nếu vị trí này chưa có trong danh sách ghế cũ, hãy tạo ghế mới
                if (!existingSeatMap.containsKey(currentPosition)) {
                    Seat newSeat = new Seat();
                    newSeat.setRoom(existingRoom);
                    newSeat.setSeatType(TypeOfSeat.valueOf(roomDTO.getTypeOfRoom().name()));
                    newSeat.setPosition(currentPosition);
                    newSeat.setName(currentPosition); // Tên ban đầu bằng vị trí
                    newSeat.setVIP(false);
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
            // Lấy danh sách ID của các ghế sắp bị xóa
            List<Integer> seatIdsToDelete = seatsToDelete.stream()
                    .map(Seat::getSeatID)
                    .collect(Collectors.toList());

            // Đếm xem có bao nhiêu vé đã được bán cho những ghế này
            long soldSeatCount = detailSeatRepository.countBySeat_SeatIDIn(seatIdsToDelete);

            // Nếu có vé đã bán, ném ra lỗi
            if (soldSeatCount > 0) {
                throw new Exception(String.format(
                        "Cannot reduce room size. At least %d seats that would be removed have already been sold in past schedules.",
                        soldSeatCount
                ));
            }
        }
        // === BƯỚC 4: THỰC THI THAY ĐỔI ===
        if (!seatsToDelete.isEmpty()) {
            seatRepository.deleteAllInBatch(seatsToDelete);
        }
        if (!seatsToCreate.isEmpty()) {
            seatRepository.saveAll(seatsToCreate);
        }

        // === BƯỚC 5: CẬP NHẬT THÔNG TIN PHÒNG ===
        existingRoom.setName(roomDTO.getName());
        existingRoom.setTypeOfRoom(roomDTO.getTypeOfRoom());
        existingRoom.setStatus(roomDTO.getStatus());
        existingRoom.setRow(newRows);
        existingRoom.setColumn(newCols);

        Room finalUpdatedRoom = roomRepository.save(existingRoom);

        return convertToDTO(finalUpdatedRoom);
    }
}