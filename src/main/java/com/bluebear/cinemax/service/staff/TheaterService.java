package com.bluebear.cinemax.service.staff;

import com.bluebear.cinemax.dto.TheaterDTO;
import com.bluebear.cinemax.entity.Theater;

import java.util.List;

public interface TheaterService {
    public TheaterDTO getTheaterById(Integer id);
    public List<TheaterDTO> findAllTheaters();
}
