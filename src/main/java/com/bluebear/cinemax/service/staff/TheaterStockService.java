package com.bluebear.cinemax.service.staff;

import com.bluebear.cinemax.dto.TheaterStockDTO;
import com.bluebear.cinemax.entity.TheaterStock;

import java.util.List;

public interface TheaterStockService {
    public List<TheaterStockDTO> findByTheaterId(Integer theaterId);
    public TheaterStockDTO getTheaterStockById(Integer id);

    public void saveTheaterStock(TheaterStockDTO TheaterStockDTO);

    public List<TheaterStockDTO> findAllTheaterStock();

    public List<TheaterStockDTO> findByItemName(String itemName, Integer theaterId);

    public TheaterStockDTO findById(Integer id);

    public boolean isDeleted(Integer id);
}
