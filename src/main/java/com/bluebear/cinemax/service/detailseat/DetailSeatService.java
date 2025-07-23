package com.bluebear.cinemax.service.detailseat;

import com.bluebear.cinemax.dto.DetailSeatDTO;
import com.bluebear.cinemax.entity.DetailSeat;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface DetailSeatService {

    DetailSeatDTO createDetailSeat(DetailSeatDTO dto);

    DetailSeatDTO getDetailSeatById(Integer id);

    List<DetailSeatDTO> getAllDetailSeats();

    DetailSeatDTO updateDetailSeat(Integer id, DetailSeatDTO dto);

    void deleteDetailSeat(Integer id);

    Page<DetailSeatDTO> getDetailSeatsByScheduleId(Integer scheduleId, Pageable pageable);

    int countDetailSeatByScheduleId(int scheduleId);

    public boolean hasCustomerWatched(int customerId, int movieId);

    DetailSeatDTO toDTO(DetailSeat entity);

    DetailSeat toEntity(DetailSeatDTO dto);
}
