package com.bluebear.cinemax.repository.cashier;

import com.bluebear.cinemax.entity.Genre;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;

import java.util.List;


public interface GenereRepository extends JpaRepository<Genre, Integer> {
    List<Genre> findAll();
}
