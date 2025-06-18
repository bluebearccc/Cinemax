package com.bluebear.cinemax.service.staff;

import com.bluebear.cinemax.dto.Detail_FDDTO;
import com.bluebear.cinemax.entity.Detail_FD;

import java.util.List;

public interface DetaillFD_Service {
    public List<Detail_FDDTO> findByTheaterStockID(Integer id);
}

