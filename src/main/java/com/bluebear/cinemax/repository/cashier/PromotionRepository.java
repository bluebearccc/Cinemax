package com.bluebear.cinemax.repository.cashier;

import com.bluebear.cinemax.entity.Promotion;
import org.springframework.data.repository.Repository;

public interface PromotionRepository extends Repository<Promotion, Integer> {
    Promotion findPromotionByPromotionIdAndStatus(Integer promotionId, Promotion.PromotionStatus status);
}
