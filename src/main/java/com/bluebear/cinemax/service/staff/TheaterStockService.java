package com.bluebear.cinemax.service.staff;

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

    public boolean isDeleted(Integer id);


    Page<TheaterStockDTO> getAllTheaterStock(Pageable pageable);
    Page<TheaterStockDTO> findByTheaterId(Integer theaterId, Pageable pageable);
    Page<TheaterStockDTO> findByItemName(String itemName, Pageable pageable);
    Page<TheaterStockDTO> findByTheaterIdAndItemName(Integer theaterId, String itemName, Pageable pageable);
}
