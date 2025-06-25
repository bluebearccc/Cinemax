package com.bluebear.cinemax.service;

import com.bluebear.cinemax.dto.TheaterDTO;
import com.bluebear.cinemax.dto.TheaterStockDTO;
import com.bluebear.cinemax.entity.Theater;
import com.bluebear.cinemax.entity.TheaterStock;
import com.bluebear.cinemax.enumtype.TheaterStock_Status;
import com.bluebear.cinemax.repository.TheaterStockRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TheaterStockService {

    @Autowired
    private TheaterStockRepository theaterStockRepository;

    public List<TheaterStockDTO> getAvailableTheaterStockByTheater(Integer theaterId) {
       return theaterStockRepository.findByTheater_TheaterIDAndStatus(theaterId, TheaterStock_Status.Active)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private TheaterStockDTO convertToDTO(TheaterStock theaterStock) {
        if (theaterStock == null) {
            return null;
        }
        return TheaterStockDTO.builder()
                .theaterStockId(theaterStock.getStockID())
                .theater(convertToTheaterDTO(theaterStock.getTheater()))
                .foodName(theaterStock.getItemName())
                .image(theaterStock.getImage())
                .quantity(theaterStock.getQuantity())
                .unitPrice(theaterStock.getPrice())
                .status(theaterStock.getStatus().name())
                .build();
    }

    private TheaterDTO convertToTheaterDTO(Theater theater) {
        if (theater == null) {
            return null;
        }
        return TheaterDTO.builder()
                .theaterID(theater.getTheaterID())
                .theaterName(theater.getTheaterName())
                .address(theater.getAddress())
                .image(theater.getImage())
                .roomQuantity(theater.getRoomQuantity())
                .status(theater.getStatus())
                .build();
    }

    public Page<TheaterStockDTO> findAvailableByTheaterId(Integer theaterId, Pageable pageable) {
        Page<TheaterStock> theaterStocks = theaterStockRepository.findByTheater_TheaterIDAndStatus(
                theaterId, TheaterStock_Status.Active, pageable
        );
        return theaterStocks.map(this::convertToDTO);
    }

    public Page<TheaterStockDTO> getAvailableTheaterStockByTheater(Integer theaterId, Pageable pageable) {
        return theaterStockRepository.findByTheater_TheaterIDAndStatus(theaterId, TheaterStock_Status.Active, pageable)
                .map(this::convertToDTO);
    }

    public Page<TheaterStockDTO> findAvailableByTheaterIdAndKeyword(Integer theaterId, String keyword, Pageable pageable) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAvailableTheaterStockByTheater(theaterId, pageable);
        }
        Page<TheaterStock> theaterStocks = theaterStockRepository.findByTheater_TheaterIDAndItemNameContainingIgnoreCaseAndStatus(
                theaterId, keyword, TheaterStock_Status.Active, pageable
        );
        return theaterStocks.map(this::convertToDTO);
    }
}
