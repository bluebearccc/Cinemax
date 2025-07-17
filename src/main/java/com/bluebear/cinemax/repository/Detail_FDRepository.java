package com.bluebear.cinemax.repository;

import com.bluebear.cinemax.entity.Detail_FD;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface Detail_FDRepository extends JpaRepository<Detail_FD, Integer> {


}
