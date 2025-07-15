package com.bluebear.cinemax.service.promotion;

import com.bluebear.cinemax.dto.PromotionDTO;

import java.util.Map;
import java.util.Optional;

public interface PromotionService {
     Optional<PromotionDTO> validatePromotionCode(String code);
    Map<String, Object> checkPromotionCode(String code, double totalAmount);
}
