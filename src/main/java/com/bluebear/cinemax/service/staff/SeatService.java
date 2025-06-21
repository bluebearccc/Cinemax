package com.bluebear.cinemax.service.staff;

import com.bluebear.cinemax.dto.SeatDTO;

import java.util.List;

public interface SeatService {
    public List<SeatDTO> findAllByRoomId(Integer id);
}
