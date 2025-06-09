package com.bluebear.cinemax.repository.staff;

import com.bluebear.cinemax.entity.TheaterStock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TheaterStockRepository extends JpaRepository<TheaterStock, Integer> {

    @Query(value = "SELECT * FROM Theater_Stock ts " +
           "WHERE ts.TheaterID = :theaterId",
           nativeQuery = true)
    List<TheaterStock> findByTheater_TheaterId(@Param("theaterId") Integer theaterId);
    
    @Query(value = "SELECT * FROM Theater_Stock ts WHERE LOWER(ts.FoodName) LIKE LOWER(CONCAT('%', :itemName, '%'))",
       nativeQuery = true)
    List<TheaterStock> findByItemNameContainingIgnoreCase(@Param("itemName") String itemName);
}