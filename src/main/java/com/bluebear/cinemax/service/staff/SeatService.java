package com.bluebear.cinemax.service.staff;

import com.bluebear.cinemax.dto.SeatDTO;
import com.bluebear.cinemax.dto.SeatUpdateRequest;
import com.bluebear.cinemax.entity.Seat;

import java.util.List;

public interface SeatService {
    public List<SeatDTO> findAllByRoomId(Integer id);
    void updateSeatsVipAndPrice(SeatUpdateRequest request) throws Exception;
    List<SeatDTO> deleteSeatById(Integer seatId) throws Exception;
    void resetSeatNamesInRoom(Integer roomId);
}
