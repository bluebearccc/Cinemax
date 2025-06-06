package com.bluebear.cinemax.service;

import com.bluebear.cinemax.dto.Detail_FDRepository;
import com.bluebear.cinemax.dto.TheaterStockRepository;
import com.bluebear.cinemax.entity.TheaterStock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TheaterStockServiceImpl implements TheaterStockService{
    @Autowired
    TheaterStockRepository theaterStockRepository;
    @Autowired
    private Detail_FDRepository detail_FDRepository;

    public List<TheaterStock> findByTheaterId(Integer theaterId) {
        return theaterStockRepository.findByTheater_TheaterId(theaterId);
    }

    public TheaterStock getTheaterStockById(Integer id) {
        return theaterStockRepository.findById(id).orElse(null);
    }

    public void saveTheaterStock(TheaterStock theaterStock) {
        theaterStockRepository.save(theaterStock);
    }

    public List<TheaterStock> findAllTheaterStock() {
        return theaterStockRepository.findAll();
    }

    public List<TheaterStock> findByItemName(String itemName){
        return theaterStockRepository.findByItemNameContainingIgnoreCase(itemName);
    }

    @Override
    public TheaterStock findById(Integer id) {
        return theaterStockRepository.findById(id).orElse(null);
    }

    public boolean isDeleted(Integer id){
        try{
            theaterStockRepository.deleteById(id);
            return true;
        } catch (DataIntegrityViolationException e) {
            return false;
        }
    }
}

