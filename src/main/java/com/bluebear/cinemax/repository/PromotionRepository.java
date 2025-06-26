package com.bluebear.cinemax.repository;

import com.bluebear.cinemax.entity.Promotion;
import com.bluebear.cinemax.enumtype.Promotion_Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;

public interface PromotionRepository extends JpaRepository<Promotion, Integer> {

    @Query("SELECT p FROM Promotion p WHERE p.status = :status AND p.startTime <= :now AND p.endTime >= :now")
    List<Promotion> findActivePromotions(@Param("status") Promotion_Status status, @Param("now") LocalDateTime now);

}
