package com.bluebear.cinemax.service.theater;

import com.bluebear.cinemax.dto.TheaterDTO;
import com.bluebear.cinemax.entity.Theater;
import org.springframework.data.domain.Page;

import java.util.List;

public interface TheaterService {
    TheaterDTO createTheater(TheaterDTO dto);

    TheaterDTO updateTheater(Integer id, TheaterDTO dto);

    void deleteTheater(Integer id);

    TheaterDTO getTheaterById(Integer id);

    TheaterDTO getTheaterByIdWithRateCounts(Integer id);

    Page<TheaterDTO> getAllTheaters();

    TheaterDTO toDTO(com.bluebear.cinemax.entity.Theater entity);

    com.bluebear.cinemax.entity.Theater toEntity(TheaterDTO dto);
}
