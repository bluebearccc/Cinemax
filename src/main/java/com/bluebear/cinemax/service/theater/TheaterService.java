package com.bluebear.cinemax.service.theater;

import com.bluebear.cinemax.dto.TheaterDTO;
import com.bluebear.cinemax.entity.Theater;
import com.bluebear.cinemax.enumtype.Theater_Status;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    public Page<TheaterDTO> findByKeywordAndStatusPaginated(String keyword, String status, Pageable pageable);
    public Page<TheaterDTO> findByKeywordPaginated(String keyword, Pageable pageable);
    public Page<TheaterDTO> findByStatusPaginated(String status, Pageable pageable);
    public Page<TheaterDTO> findAllPaginated(Pageable pageable);
    public void updateTheater(TheaterDTO theaterDTO) throws IOException;
    TheaterDTO getTheaterByName(String name);

}

