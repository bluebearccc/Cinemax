package com.bluebear.cinemax.repository;

import com.bluebear.cinemax.entity.Theater;
import org.springframework.data.jpa.repository.JpaRepository;


public interface TheaterRepository extends JpaRepository<Theater, Integer> {
    Theater findByTheaterId(Integer theaterId);
}
