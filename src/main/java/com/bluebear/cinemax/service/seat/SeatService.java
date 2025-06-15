package com.bluebear.cinemax.service.seat;

import com.bluebear.cinemax.dto.SeatDTO;
import com.bluebear.cinemax.entity.Seat;

import java.util.List;

public interface SeatService {
    // --- CRUD ---
    SeatDTO createSeat(SeatDTO dto);

    SeatDTO updateSeat(Integer seatID, SeatDTO dto);

    void deleteSeat(Integer seatID);

    SeatDTO getSeatById(Integer seatID);

    List<SeatDTO> getAllSeats();

    // --- Custom ---
    List<SeatDTO> getSeatsByRoomId(Integer roomID);

    // --- Mapping ---
    SeatDTO toDTO(Seat entity);

    Seat toEntity(SeatDTO dto);
}
