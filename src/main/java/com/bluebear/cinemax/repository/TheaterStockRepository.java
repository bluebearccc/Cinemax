package com.bluebear.cinemax.repository;

import com.bluebear.cinemax.entity.TheaterStock;
import com.bluebear.cinemax.enumtype.TheaterStock_Status;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface TheaterStockRepository extends JpaRepository<TheaterStock, Integer> {
    List<TheaterStock> findByTheater_TheaterIDAndStatus(Integer theaterId, TheaterStock_Status status);

    @Query(value = "SELECT * FROM Theater_Stock ts " +
           "WHERE ts.TheaterID = :theaterId",
           nativeQuery = true)
    List<TheaterStock> findByTheater_TheaterId(@Param("theaterId") Integer theaterId);

    @Query(value = "SELECT * FROM Theater_Stock ts WHERE LOWER(ts.FoodName) LIKE LOWER(CONCAT('%', :itemName, '%')) AND ts.TheaterID = :theaterId",
       nativeQuery = true)
    List<TheaterStock> findByItemNameContainingIgnoreCase(@Param("itemName") String itemName, @Param("theaterId") Integer theaterId);
    Page<TheaterStock> findByTheater_TheaterIDAndStatus(Integer theaterId, TheaterStock_Status theaterStockStatus, Pageable pageable);

    Page<TheaterStock> findByTheater_TheaterIDAndItemNameContainingIgnoreCaseAndStatus(
            Integer theaterId, String itemName, TheaterStock_Status status, Pageable pageable);

    List<TheaterStock> findByStatus(TheaterStock_Status theaterStockStatus);
}