package com.bluebear.cinemax.service.seat;

import com.bluebear.cinemax.dto.SeatDTO;
import com.bluebear.cinemax.entity.Seat;
import org.springframework.data.domain.Page;

import java.util.List;

public interface SeatService {
    List<SeatDTO> getSeatsWithStatus(Integer roomId, Integer scheduleId);

    SeatDTO createSeat(SeatDTO dto);

    SeatDTO updateSeat(Integer seatID, SeatDTO dto);

    void deleteSeat(Integer seatID);

    SeatDTO getSeatById(Integer seatID);

    Page<SeatDTO> getAllSeats();

    Page<SeatDTO> getSeatsByRoomId(Integer roomID);

    SeatDTO toDTO(Seat entity);

    Seat toEntity(SeatDTO dto);

    public List<SeatDTO> findAllByRoomId(Integer id);

    List<SeatDTO> getSeatsByIds(List<Integer> selectedSeatIds);

}
