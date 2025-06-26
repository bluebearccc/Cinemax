package com.bluebear.cinemax.service;

import com.bluebear.cinemax.dto.SeatDTO;
import com.bluebear.cinemax.entity.Seat;
import com.bluebear.cinemax.repository.SeatRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SeatService {

    @Autowired
    private SeatRepository seatRepository;

    public List<SeatDTO> getSeatsByRoomId(Integer roomId) {
        List<Seat> seats = seatRepository.findByRoom_RoomID(roomId);
        return seats.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private SeatDTO convertToDTO(Seat seat) {
        return SeatDTO.builder()
                .seatID(seat.getSeatID())
                .roomID(seat.getRoom().getRoomID())
                .seatType(seat.getSeatType())
                .position(seat.getPosition())
                .isVIP(seat.isVIP())
                .unitPrice(seat.getUnitPrice())
                .status(seat.getStatus())
                .build();
    }

    public List<SeatDTO> getSeatsByIds(List<Integer> selectedSeatIds) {
        return seatRepository.findAllById(selectedSeatIds)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
}