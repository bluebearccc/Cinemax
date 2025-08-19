package com.bluebear.cinemax.service.detail_fd;

import com.bluebear.cinemax.dto.Detail_FDDTO;
import com.bluebear.cinemax.dto.RevenueDataDTO;

import java.util.List;

public interface DetaillFD_Service {
    public List<Detail_FDDTO> findByTheaterStockID(Integer id);
    public List<Detail_FDDTO> getAllFoodSalesForTheater(Integer theaterId, int year, int month);
    public RevenueDataDTO getRevenueByItemForMonth(Integer theaterId, int year, int month);
}
