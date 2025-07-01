package com.bluebear.cinemax.repository;
import com.bluebear.cinemax.entity.DetailFD;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DetailFDRepository extends JpaRepository<DetailFD, Integer> {
}
