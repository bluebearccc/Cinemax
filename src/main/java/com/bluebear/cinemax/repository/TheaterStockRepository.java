package com.bluebear.cinemax.repository;

import com.bluebear.cinemax.entity.TheaterStock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TheaterStockRepository extends JpaRepository<TheaterStock, Integer> {
    List<TheaterStock> findByStatus(String status);
    // Tìm kiếm combo theo ID
    TheaterStock findByTheaterStockID(Integer theaterStockID);
}
