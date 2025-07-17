package com.bluebear.cinemax.repository;

import com.bluebear.cinemax.entity.Theater;
import com.bluebear.cinemax.enumtype.Theater_Status;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TheaterRepository extends JpaRepository<Theater, Integer> {
    Page<Theater> findByStatus(Theater_Status status, Pageable pageable);

    Theater findByTheaterNameContainingIgnoreCase(String theaterName);
}
