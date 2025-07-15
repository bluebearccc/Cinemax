package com.bluebear.cinemax.service.seat;

import com.bluebear.cinemax.dto.SeatDTO;
import com.bluebear.cinemax.entity.Seat;

import java.util.List;

public interface SeatService {
    List<SeatDTO> toSeatDTOList(List<Seat> seats);
    List<SeatDTO> getSeatsWithStatus(Integer roomId, Integer scheduleId);
}
