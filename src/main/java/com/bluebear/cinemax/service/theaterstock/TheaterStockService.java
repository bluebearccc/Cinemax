package com.bluebear.cinemax.service.theaterstock;

import com.bluebear.cinemax.dto.TheaterStockDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface TheaterStockService {
    public List<TheaterStockDTO> findByTheaterId(Integer theaterId);

    public TheaterStockDTO getTheaterStockById(Integer id);

    public void saveTheaterStock(TheaterStockDTO TheaterStockDTO);

    public List<TheaterStockDTO> findAllTheaterStock();

    public List<TheaterStockDTO> findByItemName(String itemName, Integer theaterId);

    public TheaterStockDTO findById(Integer id);

    Page<TheaterStockDTO> findAvailableByTheaterId(Integer theaterId, Pageable pageable);

    Page<TheaterStockDTO> getAvailableTheaterStockByTheater(Integer theaterId, Pageable pageable);

    Page<TheaterStockDTO> findAvailableByTheaterIdAndKeyword(Integer theaterId, String keyword, Pageable pageable);

    List<TheaterStockDTO> getAvailableTheaterStockByTheater(Integer theaterId);

    public boolean isDeleted(Integer id);
}
