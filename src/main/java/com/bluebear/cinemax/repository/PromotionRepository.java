package com.bluebear.cinemax.repository;

import com.bluebear.cinemax.entity.Promotion;
import com.bluebear.cinemax.enumtype.Promotion_Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PromotionRepository extends JpaRepository<Promotion, Integer> {


    // Check if promotion code exists (for validation)
    boolean existsByPromotionCode(String promotionCode);

    // Search vouchers by code or status
    @Query("SELECT v FROM Promotion v WHERE " +
            "(:keyword IS NULL OR :keyword = '' OR LOWER(v.promotionCode) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
            "(:status IS NULL OR v.status = :status)")
    List<Promotion> searchVouchers(@Param("keyword") String keyword, @Param("status") Promotion_Status status);

    // Count vouchers by status (using enum)
    long countByStatus(Promotion_Status status);


    // Get average discount for active vouchers
    @Query("SELECT AVG(v.discount) FROM Promotion v WHERE v.status = :status")
    Double getAverageDiscount(@Param("status") Promotion_Status status);


}