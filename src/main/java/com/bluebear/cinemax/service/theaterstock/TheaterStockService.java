package com.bluebear.cinemax.service.theaterstock;

import com.bluebear.cinemax.dto.TheaterStockDTO;
import com.bluebear.cinemax.entity.TheaterStock;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public interface TheaterStockService {
    TheaterStockDTO convertToDTO(TheaterStock theaterStock);

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

    Page<TheaterStockDTO> findByTheaterIdAndItemName(Integer theaterId, String trim, Pageable pageable);
    Page<TheaterStockDTO> findByItemName(String trim, Pageable pageable);
    Page<TheaterStockDTO> findByTheaterId(Integer theaterId, Pageable pageable);
    Page<TheaterStockDTO> getAllTheaterStock(Pageable pageable);
    public Optional<TheaterStockDTO> findFirstByItemName(String itemName);
    public boolean itemExistsInTheater(String itemName, Integer theaterId);
    public String saveImage(MultipartFile img) throws IOException;
    public List<TheaterStockDTO> findAllByItemName(String itemName);
    void updateItemAcrossAllTheaters(TheaterStockDTO formDataWithChanges) throws IOException;

}
