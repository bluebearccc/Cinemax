package com.bluebear.cinemax.service;

import com.bluebear.cinemax.dto.PromotionDTO;
import com.bluebear.cinemax.entity.Promotion;
import com.bluebear.cinemax.enumtype.Promotion_Status;
import com.bluebear.cinemax.repository.PromotionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PromotionService {

    @Autowired
    private PromotionRepository promotionRepository;

    public List<PromotionDTO> getActivePromotions() {
        return promotionRepository.findActivePromotions(Promotion_Status.Available, LocalDateTime.now())
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public PromotionDTO getPromotionById(Integer promotionId) {
        return promotionRepository.findById(promotionId)
                .map(this::convertToDTO)
                .orElse(null);
    }

    private PromotionDTO convertToDTO(Promotion promotion) {
        return PromotionDTO.builder()
                .promotionID(promotion.getPromotionID())
                .promotionCode(promotion.getPromotionCode())
                .discount(promotion.getDiscount())
                .startTime(promotion.getStartTime())
                .endTime(promotion.getEndTime())
                .quantity(promotion.getQuantity())
                .status(promotion.getStatus())
                .build();
    }
}