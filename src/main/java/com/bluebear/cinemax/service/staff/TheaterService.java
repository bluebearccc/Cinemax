package com.bluebear.cinemax.service.staff;

import com.bluebear.cinemax.dto.TheaterDTO;
import com.bluebear.cinemax.entity.Theater;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface TheaterService {
    public TheaterDTO getTheaterById(Integer id);
    public List<TheaterDTO> findAllTheaters();
    public List<TheaterDTO> findAllTheaters(String status);
    public List<TheaterDTO> findAllTheatersByName(String keyword);
    TheaterDTO addTheater(TheaterDTO theaterDTO, MultipartFile imageFile) throws IOException, Exception;

}
