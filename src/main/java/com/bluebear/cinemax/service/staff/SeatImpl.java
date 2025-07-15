package com.bluebear.cinemax.service.staff;

import com.bluebear.cinemax.config.EmailService;
import com.bluebear.cinemax.dto.SeatDTO;
import com.bluebear.cinemax.dto.SeatUpdateRequest;
import com.bluebear.cinemax.entity.DetailSeat;
import com.bluebear.cinemax.entity.Seat;
import com.bluebear.cinemax.enumtype.Seat_Status;
import com.bluebear.cinemax.repository.DetailSeatRepository;
import com.bluebear.cinemax.repository.RoomRepository;
import com.bluebear.cinemax.repository.SeatRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SeatImpl implements SeatService {

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private SeatRepository seatRepository;

    @Autowired
    private DetailSeatRepository detailSeatRepository;

    @Autowired
    private EmailService emailService;

    private SeatDTO convertToDTO(Seat seat) {
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

    private Seat convertToEntity(SeatDTO seatDTO) {
        if (seatDTO == null) {
            return null;
        }

        // B1: Tìm kiếm Seat đã tồn tại trong database dựa trên ID từ DTO.
        // Nếu không có ID (trường hợp tạo mới), thì tạo đối tượng mới.
        Seat seat = seatDTO.getSeatID() != null ?
                seatRepository.findById(seatDTO.getSeatID()).orElse(new Seat()) :
                new Seat();

        // B2: Cập nhật các thuộc tính của đối tượng Seat đã được quản lý (persistent)
        seat.setSeatType(seatDTO.getSeatType());
        seat.setPosition(seatDTO.getPosition());
        seat.setName(seatDTO.getName());
        seat.setVIP(seatDTO.getIsVIP());
        seat.setUnitPrice(seatDTO.getUnitPrice());
        seat.setStatus(seatDTO.getStatus());

        // Gán Room (đảm bảo Room được lấy từ DB)
        if (seatDTO.getRoomID() != null && (seat.getRoom() == null || !seat.getRoom().getRoomID().equals(seatDTO.getRoomID()))) {
            roomRepository.findById(seatDTO.getRoomID())
                    .ifPresent(seat::setRoom);
        }

        return seat; // Trả về đối tượng Seat đã được quản lý hoặc sẵn sàng để lưu
    }

    @Override
    public List<SeatDTO> findAllByRoomId(Integer id) {
        List<Seat> seats = seatRepository.findByRoom_RoomID(id);
        List<SeatDTO> seatDTOS = new ArrayList<>();
        for (Seat seat : seats) {
            seatDTOS.add(convertToDTO(seat));
        }
        return seatDTOS;
    }

    @Override
    @Transactional
    public void updateSeatsVipAndPrice(SeatUpdateRequest request) throws Exception {
        if (request.getRoomId() == null) {
            throw new Exception("Room ID is required.");
        }
        if (request.getVipPrice() != null && request.getNonVipPrice() != null) {
            if (request.getVipPrice() < request.getNonVipPrice() + 40000) {
                throw new Exception("Price for VIP seats cannot be less than the price for non-VIP seats plus 40,000. Please check and try again.");
            }
        }
        List<Seat> seatsInRoom = seatRepository.findByRoom_RoomID(request.getRoomId());
        List<Integer> vipSeatIds = request.getVipSeatIds() != null ? request.getVipSeatIds() : new ArrayList<>();

        Map<Integer, String> newPositions = request.getPositions();

        if (newPositions != null && !newPositions.isEmpty()) {
            long distinctPositions = newPositions.values().stream().distinct().count();
            if (distinctPositions < newPositions.size()) {
                throw new Exception("Duplicate position values found in the request. Please check and try again.");
            }

            for (Seat seat : seatsInRoom) {
                if (newPositions.containsKey(seat.getSeatID())) {
                    String newPositionValue = newPositions.get(seat.getSeatID());
                    seat.setPosition(newPositionValue);
                }
            }
        }

        for (Seat seat : seatsInRoom) {
            if (vipSeatIds.contains(seat.getSeatID())) {
                seat.setVIP(true);
                seat.setUnitPrice(request.getVipPrice());
            } else {
                seat.setVIP(false);
                seat.setUnitPrice(request.getNonVipPrice());
            }
        }

        seatRepository.saveAll(seatsInRoom);
    }

    @Override
    @Transactional
    public List<SeatDTO> deleteSeatById(Integer seatId) throws Exception {

        // === BƯỚC 1: KIỂM TRA RÀNG BUỘC TRONG DetailSeat ===
        // Gọi phương thức mới từ repository để đếm số lần ghế này đã được đặt vé
        long bookingCount = detailSeatRepository.countBySeat_SeatID(seatId);

        // Nếu ghế đã có trong bất kỳ hóa đơn nào (đã được đặt), ném ra lỗi và không cho xóa
        if (bookingCount > 0) {
            throw new Exception("Cannot delete this seat because it is part of existing bookings. Consider setting its status to 'Inactive' instead.");
        }

        // === BƯỚC 2: TIẾN HÀNH XÓA NẾU HỢP LỆ ===
        // Chỉ thực hiện các bước dưới đây nếu việc kiểm tra ở trên đã vượt qua

        // Tìm ghế để lấy ID của phòng trước khi xóa
        Seat seatToDelete = seatRepository.findById(seatId)
                .orElseThrow(() -> new Exception("Seat not found with id: " + seatId));
        Integer roomId = seatToDelete.getRoom().getRoomID();

        // Xóa ghế
        seatRepository.delete(seatToDelete);

        // Đánh lại tên các ghế còn lại
        this.resetSeatNamesInRoom(roomId);

        // Trả về danh sách ghế mới nhất của phòng
        List<Seat> updatedSeats = seatRepository.findByRoom_RoomIDOrderByPositionAsc(roomId);
        return updatedSeats.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    @Override
    @Transactional
    public void resetSeatNamesInRoom(Integer roomId) {
        List<Seat> allSeats = seatRepository.findByRoom_RoomIDOrderByPositionAsc(roomId);

        Map<Character, List<Seat>> seatsByRow = new LinkedHashMap<>();
        for (Seat seat : allSeats) {
            char rowChar = seat.getPosition().charAt(0);
            seatsByRow.computeIfAbsent(rowChar, k -> new ArrayList<>()).add(seat);
        }

        for (List<Seat> rowSeats : seatsByRow.values()) {
            for (int i = 0; i < rowSeats.size(); i++) {
                Seat currentSeat = rowSeats.get(i);
                char rowChar = currentSeat.getPosition().charAt(0);

                String newName = rowChar + String.valueOf(i + 1);
                currentSeat.setName(newName);
            }
        }

        seatRepository.saveAll(allSeats);
    }
    @Transactional
    public void updateSeatsInRoom(SeatUpdateRequest request) {
        Integer roomId = request.getRoomId();
        List<Integer> vipSeatIds = request.getVipSeatIds() != null ? request.getVipSeatIds() : new ArrayList<>();
        Map<Integer, Seat_Status> newStatuses = request.getSeatStatuses();

        List<Seat> seatsInRoom = seatRepository.findByRoom_RoomID(roomId);

        for (Seat seat : seatsInRoom) {
            // Lấy trạng thái cũ và mới
            Seat_Status oldStatus = seat.getStatus();
            Seat_Status newStatus = newStatuses.get(seat.getSeatID());

            // --- LOGIC KIỂM TRA TRƯỚC KHI CẬP NHẬT ---
            // Chỉ kiểm tra nếu trạng thái thực sự thay đổi từ Active sang Inactive
            if (newStatus != null && oldStatus == Seat_Status.Active && newStatus == Seat_Status.Inactive) {

                // 1. Tìm các vé trong tương lai bị ảnh hưởng
                List<DetailSeat> futureBookings = detailSeatRepository.findFutureBookingsBySeatId(seat.getSeatID(), LocalDateTime.now());

                // 2. Nếu có, gửi email thông báo
                if (!futureBookings.isEmpty()) {
                    emailService.sendSeatCancellationNotice(futureBookings);}
            }

            // --- TIẾN HÀNH CẬP NHẬT DỮ LIỆU ---
            // Cập nhật VIP và giá vé
            if (vipSeatIds.contains(seat.getSeatID())) {
                seat.setVIP(true);
                seat.setUnitPrice(request.getVipPrice());
            } else {
                seat.setVIP(false);
                seat.setUnitPrice(request.getNonVipPrice());
            }

            // Cập nhật trạng thái
            if (newStatus != null) {
                seat.setStatus(newStatus);
            }
        }

        seatRepository.saveAll(seatsInRoom);
    }
}