package com.bluebear.cinemax.service.theater;

import com.bluebear.cinemax.dto.TheaterDTO;
import com.bluebear.cinemax.entity.Theater;

import java.util.List;

public interface TheaterService {
    // --- CRUD ---
    TheaterDTO createTheater(TheaterDTO dto);

    TheaterDTO updateTheater(Integer id, TheaterDTO dto);

    void deleteTheater(Integer id);

    TheaterDTO getTheaterById(Integer id);

    List<TheaterDTO> getAllTheaters();

    // --- Mapping ---
    TheaterDTO toDTO(Theater entity);

    Theater toEntity(TheaterDTO dto);
}
