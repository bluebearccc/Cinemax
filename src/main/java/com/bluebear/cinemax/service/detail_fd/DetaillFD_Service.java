package com.bluebear.cinemax.service.detail_fd;

import com.bluebear.cinemax.dto.Detail_FDDTO;

import java.util.List;

public interface DetaillFD_Service {
    public List<Detail_FDDTO> findByTheaterStockID(Integer id);
}

