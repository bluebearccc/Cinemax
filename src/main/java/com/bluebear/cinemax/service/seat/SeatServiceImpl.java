package com.bluebear.cinemax.service.seat;

import com.bluebear.cinemax.enumtype.Seat_Status;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import com.bluebear.cinemax.dto.SeatDTO;
import com.bluebear.cinemax.entity.Room;
import com.bluebear.cinemax.entity.Seat;
import com.bluebear.cinemax.repository.RoomRepository;
import com.bluebear.cinemax.repository.SeatRepository;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class SeatServiceImpl implements SeatService {
    @Autowired
    private SeatRepository seatRepository;

    @Autowired
    private RoomRepository roomRepository;

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
        seat.setIsVIP(dto.getIsVIP());
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

    public SeatDTO toDTO(Seat entity) {
        SeatDTO dto = new SeatDTO();
        dto.setSeatID(entity.getSeatID());
        dto.setRoomID(entity.getRoom().getRoomID());
        dto.setSeatType(entity.getSeatType());
        dto.setPosition(entity.getPosition());
        dto.setIsVIP(entity.getIsVIP());
        dto.setUnitPrice(entity.getUnitPrice());
        dto.setStatus(entity.getStatus());

        return dto;
    }

    public Seat toEntity(SeatDTO dto) {
        Seat seat = new Seat();
        seat.setSeatID(dto.getSeatID());
        seat.setSeatType(dto.getSeatType());
        seat.setPosition(dto.getPosition());
        seat.setIsVIP(dto.getIsVIP());
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

