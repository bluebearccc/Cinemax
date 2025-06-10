package com.bluebear.cinemax.repository.cashier;

import com.bluebear.cinemax.entity.Promotion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PromotionRepository extends JpaRepository<Promotion, Integer> {
    // Tìm khuyến mãi theo mã
    Optional<Promotion> findByPromotionCodeAndStatus(String promotionCode, Promotion.PromotionStatus status);

    // Tìm khuyến mãi còn hiệu lực
    List<Promotion> findByStatusAndStartTimeBeforeAndEndTimeAfter(Promotion.PromotionStatus status, LocalDateTime currentTime1, LocalDateTime currentTime2);

    // Tìm khuyến mãi còn số lượng
    List<Promotion> findByStatusAndQuantityGreaterThan(Promotion.PromotionStatus status, Integer minQuantity);
}
