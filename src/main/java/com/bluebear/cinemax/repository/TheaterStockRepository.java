package com.bluebear.cinemax.repository;

import com.bluebear.cinemax.entity.TheaterStock;
import com.bluebear.cinemax.enumtype.TheaterStock_Status;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface TheaterStockRepository extends JpaRepository<TheaterStock, Integer> {
    List<TheaterStock> findByTheater_TheaterIDAndStatus(Integer theaterId, TheaterStock_Status status);

    Page<TheaterStock> findByTheater_TheaterIDAndStatus(Integer theaterId, TheaterStock_Status theaterStockStatus, Pageable pageable);

    Page<TheaterStock> findByTheater_TheaterIDAndItemNameContainingIgnoreCaseAndStatus(
            Integer theaterId, String itemName, TheaterStock_Status status, Pageable pageable);
}