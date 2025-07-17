package com.bluebear.cinemax.repository;

import com.bluebear.cinemax.entity.Theater;
import com.bluebear.cinemax.enumtype.Theater_Status;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TheaterRepository extends JpaRepository<Theater, Integer> {
    List<Theater> findByStatus(Theater_Status status);
    List<Theater> findByTheaterNameContainingIgnoreCase(String keyword);
    // Kiểm tra xem tên rạp đã tồn tại chưa (không phân biệt chữ hoa/thường)
    boolean existsByTheaterNameIgnoreCase(String theaterName);

    // Kiểm tra xem địa chỉ đã tồn tại chưa (không phân biệt chữ hoa/thường)
    boolean existsByAddressIgnoreCase(String address);
    Page<Theater> findByStatus(Theater_Status status, Pageable pageable);
}
