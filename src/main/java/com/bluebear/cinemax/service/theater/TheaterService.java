package com.bluebear.cinemax.service.theater;

import com.bluebear.cinemax.dto.TheaterDTO;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
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

    public List<TheaterDTO> findAllTheaters();

    public List<TheaterDTO> findAllTheaters(String status);

    public List<TheaterDTO> findAllTheatersByName(String keyword);

    TheaterDTO addTheater(TheaterDTO theaterDTO, MultipartFile imageFile) throws IOException, Exception;

}

