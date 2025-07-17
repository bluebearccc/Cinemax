package com.bluebear.cinemax.service.seat;

import com.bluebear.cinemax.entity.DetailSeat;
import com.bluebear.cinemax.entity.Invoice;
import com.bluebear.cinemax.enumtype.DetailSeat_Status;
import com.bluebear.cinemax.enumtype.Seat_Status;
import com.bluebear.cinemax.repository.DetailSeatRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import com.bluebear.cinemax.dto.SeatDTO;
import com.bluebear.cinemax.entity.Room;
import com.bluebear.cinemax.entity.Seat;
import com.bluebear.cinemax.repository.RoomRepository;
import com.bluebear.cinemax.repository.SeatRepository;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class SeatServiceImpl implements SeatService {
    @Autowired
    private DetailSeatRepository detailSeatRepository;
    @Autowired
    private SeatRepository seatRepository;

    @Autowired
    private RoomRepository roomRepository;
    public List<SeatDTO> getSeatsWithStatus(Integer roomId, Integer scheduleId) {
        List<Seat> seats = seatRepository.findByRoomRoomID(roomId);
        List<SeatDTO> seatDTOs = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        for (Seat seat : seats) {
            List<DetailSeat> relatedSeats = detailSeatRepository.findBySeatSeatIDAndScheduleScheduleIDAndStatusIn(
                    seat.getSeatID(), scheduleId,
                    List.of(DetailSeat_Status.Unpaid, DetailSeat_Status.Booked)
            );

            boolean isBooked = false;

            for (DetailSeat ds : relatedSeats) {
                Invoice invoice = ds.getInvoice();
                if (ds.getStatus() == DetailSeat_Status.Booked) {
                    isBooked = true;
                    break;
                }

                if (ds.getStatus() == DetailSeat_Status.Unpaid &&
                        invoice != null &&
                        invoice.getBookingDate() != null &&
                        invoice.getBookingDate().isAfter(now.minusMinutes(15))) {
                    // Ghế vẫn đang được giữ < 15 phút => xem là booked
                    isBooked = true;
                    break;
                }
            }

            SeatDTO dto = toDTO(seat);
            dto.setBooked(isBooked); // true nếu đang bị giữ hoặc đã thanh toán
            seatDTOs.add(dto);
        }

        return seatDTOs;
    }
    public List<SeatDTO> toSeatDTOList(List<Seat> seats) {
        return seats.stream().map(this::toDTO).toList();
    }

    public SeatDTO createSeat(SeatDTO dto) {
        Seat seat = toEntity(dto);
        return toDTO(seatRepository.save(seat));
    }

    public SeatDTO updateSeat(Integer seatID, SeatDTO dto) {
        Optional<Seat> optionalSeat = seatRepository.findById(seatID);
        Optional<Room> optionalRoom = roomRepository.findById(dto.getRoomID());
        if (optionalSeat.isEmpty()) return null;

        Seat seat = optionalSeat.get();
        seat.setSeatType(dto.getSeatType());
        seat.setPosition(dto.getPosition());
        seat.setVIP(dto.getIsVIP());
        seat.setUnitPrice(dto.getUnitPrice());
        seat.setStatus(dto.getStatus());
        if (!optionalRoom.isEmpty()) {seat.setRoom(optionalRoom.get());}

        return toDTO(seatRepository.save(seat));
    }

    public void deleteSeat(Integer seatID) {
        seatRepository.deleteById(seatID);
    }

    public SeatDTO getSeatById(Integer seatID) {
        return seatRepository.findById(seatID)
                .map(this::toDTO)
                .orElse(null);
    }

    public Page<SeatDTO> getAllSeats() {
        return seatRepository.findByStatus(Seat_Status.Active, Pageable.unpaged())
                .map(this::toDTO);
    }

    public Page<SeatDTO> getSeatsByRoomId(Integer roomID) {
        return seatRepository.findByRoom_RoomIDAndStatus(roomID, Seat_Status.Active, Pageable.unpaged())
                .map(this::toDTO);
    }

    public SeatDTO toDTO(Seat seat) {
        SeatDTO dto = new SeatDTO();
        dto.setSeatID(seat.getSeatID());
        dto.setRoomID(seat.getRoom().getRoomID());
        dto.setSeatType(seat.getSeatType() != null ? seat.getSeatType() : null);
        dto.setPosition(seat.getPosition());
        dto.setIsVIP(seat.isVIP());
        dto.setUnitPrice(seat.getUnitPrice());
        dto.setStatus(seat.getStatus() != null ? seat.getStatus() : null);
        return dto;
    }

    public Seat toEntity(SeatDTO dto) {
        Seat seat = new Seat();
        seat.setSeatID(dto.getSeatID());
        seat.setSeatType(dto.getSeatType());
        seat.setPosition(dto.getPosition());
        seat.setVIP(dto.getIsVIP());
        seat.setUnitPrice(dto.getUnitPrice());
        seat.setStatus(dto.getStatus());

        // Gắn Room từ roomID
        Optional<Room> room = roomRepository.findById(dto.getRoomID());
        room.ifPresent(seat::setRoom);

        return seat;
    }

    @Override
    public List<SeatDTO> findAllByRoomId(Integer id) {
        List<Seat> seats = seatRepository.findByRoom_RoomID(id);
        List<SeatDTO> seatDTOS = new ArrayList<>();
        for (Seat seat : seats) {
            seatDTOS.add(toDTO(seat));
        }
        return seatDTOS;
    }

    public List<SeatDTO> getSeatsByIds(List<Integer> selectedSeatIds) {
        return seatRepository.findAllById(selectedSeatIds)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }


}

