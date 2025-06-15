package com.bluebear.cinemax.repository.cashier;

import com.bluebear.cinemax.entity.TheaterStock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TheaterStockRepository extends JpaRepository<TheaterStock, Integer> {

    @Query("""
    SELECT ts FROM TheaterStock ts
    WHERE ts.theater.theaterId = :theaterId
    AND ts.status = :status
    ORDER BY ts.foodName
    """)
    List<TheaterStock> findByTheaterIdAndStatus(@Param("theaterId") Integer theaterId,
                                                @Param("status") TheaterStock.StockStatus status);

    @Query("""
    SELECT ts FROM TheaterStock ts
    WHERE ts.theater.theaterId = :theaterId
    AND ts.status = :status
    AND ts.quantity > 0
    ORDER BY ts.foodName
    """)
    List<TheaterStock> findAvailableByTheaterIdAndStatus(@Param("theaterId") Integer theaterId,
                                                         @Param("status") TheaterStock.StockStatus status);
}