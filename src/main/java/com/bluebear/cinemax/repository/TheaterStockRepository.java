package com.bluebear.cinemax.repository;

import com.bluebear.cinemax.entity.TheaterStock;
import com.bluebear.cinemax.enumtype.TheaterStock_Status;
import com.bluebear.cinemax.enumtype.Theater_Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TheaterStockRepository extends JpaRepository<TheaterStock, Integer> {
    List<TheaterStock> findByStatus(TheaterStock_Status status);
    // Tìm kiếm combo theo ID

}
