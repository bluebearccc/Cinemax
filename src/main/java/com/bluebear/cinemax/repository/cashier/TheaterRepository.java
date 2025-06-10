package com.bluebear.cinemax.repository.cashier;

import com.bluebear.cinemax.entity.Theater;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TheaterRepository extends JpaRepository<Theater, Integer> {
    // Tìm rạp active
    List<Theater> findByStatusOrderByTheaterName(Theater.TheaterStatus status);

    // Tìm rạp theo tên
    List<Theater> findByTheaterNameContainingIgnoreCaseAndStatus(String theaterName, Theater.TheaterStatus status);
}