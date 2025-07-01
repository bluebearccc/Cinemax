package com.bluebear.cinemax.service.staff;

import com.bluebear.cinemax.dto.TheaterStockDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public interface TheaterStockService {
    public List<TheaterStockDTO> findByTheaterId(Integer theaterId);
    TheaterStockDTO getTheaterStockById(Integer id);
    public void saveTheaterStock(TheaterStockDTO TheaterStockDTO);
    public List<TheaterStockDTO> findAllTheaterStock();
    public List<TheaterStockDTO> findByItemName(String itemName, Integer theaterId);
    public boolean isDeleted(Integer id);
    public boolean itemExistsInTheater(String itemName, Integer theaterId);
    public Page<TheaterStockDTO> getAllTheaterStock(Pageable pageable);
    public Page<TheaterStockDTO> findByTheaterId(Integer theaterId, Pageable pageable);
    public Page<TheaterStockDTO> findByItemName(String itemName, Pageable pageable);
    public Page<TheaterStockDTO> findByTheaterIdAndItemName(Integer theaterId, String itemName, Pageable pageable);
    public Optional<TheaterStockDTO> findFirstByItemName(String itemName);
    List<TheaterStockDTO> findAllByItemName(String itemName);
    void updateItemAcrossAllTheaters(TheaterStockDTO formDataWithChanges) throws IOException;

}
