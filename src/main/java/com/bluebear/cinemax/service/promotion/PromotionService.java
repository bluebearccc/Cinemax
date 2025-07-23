package com.bluebear.cinemax.service.promotion;

import com.bluebear.cinemax.dto.PromotionDTO;
import com.bluebear.cinemax.entity.Promotion;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface PromotionService {
    PromotionDTO createPromotion(PromotionDTO dto);

    PromotionDTO getPromotionById(Integer id);

    List<PromotionDTO> getAllPromotions();

    PromotionDTO updatePromotion(Integer id, PromotionDTO dto);

    void deletePromotion(Integer id);

    PromotionDTO toDTO(Promotion promotion);

    Promotion toEntity(PromotionDTO dto);

    List<PromotionDTO> getActivePromotions();

    Optional<PromotionDTO> validatePromotionCode(String code);
    Map<String, Object> checkPromotionCode(String code, double totalAmount);

}
