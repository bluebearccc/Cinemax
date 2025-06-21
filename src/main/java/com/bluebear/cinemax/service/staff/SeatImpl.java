package com.bluebear.cinemax.service.staff;

import com.bluebear.cinemax.dto.SeatDTO;
import com.bluebear.cinemax.entity.Seat;
import com.bluebear.cinemax.repository.RoomRepository;
import com.bluebear.cinemax.repository.SeatRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
@Service
public class SeatImpl implements SeatService {

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private SeatRepository seatRepository;

    private SeatDTO convertToDTO(Seat seat) {
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

    private Seat convertToEntity(SeatDTO seatDTO) {
        if (seatDTO == null) {
            return null;
        }
        Seat seat = new Seat();
        seat.setSeatID(seatDTO.getSeatID());
        seat.setSeatType(seatDTO.getSeatType());
        seat.setPosition(seatDTO.getPosition());
        seat.setVIP(seatDTO.getIsVIP());
        seat.setUnitPrice(seatDTO.getUnitPrice());
        seat.setStatus(seatDTO.getStatus());
        if(seatDTO.getRoomID() != null) {
            roomRepository.findById(seatDTO.getRoomID())
                    .ifPresent(seat::setRoom);
        }
        return seat;
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
}
