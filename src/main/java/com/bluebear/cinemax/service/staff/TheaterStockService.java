package com.bluebear.cinemax.service.staff;

import com.bluebear.cinemax.entity.TheaterStock;

import java.util.List;

public interface TheaterStockService {
    public List<TheaterStock> findByTheaterId(Integer theaterId);
    public TheaterStock getTheaterStockById(Integer id);

    public void saveTheaterStock(TheaterStock theaterStock);

    public List<TheaterStock> findAllTheaterStock();

    public List<TheaterStock> findByItemName(String itemName, Integer theaterId);

    public TheaterStock findById(Integer id);

    public boolean isDeleted(Integer id);
}
