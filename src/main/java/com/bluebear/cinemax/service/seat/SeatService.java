package com.bluebear.cinemax.service.seat;

import com.bluebear.cinemax.dto.SeatDTO;
import com.bluebear.cinemax.dto.SeatUpdateRequest;
import com.bluebear.cinemax.entity.Seat;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public interface SeatService {
    List<Integer> getUnpaidSeatIdsForSchedule(Integer scheduleId);
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

    public void updateSeatsVipAndPrice(SeatUpdateRequest request) throws Exception;

    public List<SeatDTO> deleteSeatById(Integer seatId) throws Exception;

    public void resetSeatNamesInRoom(Integer roomId);
    public void updateSeatsInRoom(SeatUpdateRequest request);
    //

    List<SeatDTO> toSeatDTOList(List<Seat> seats);
    List<SeatDTO> getSeatsWithStatus(Integer roomId, Integer scheduleId);
}
