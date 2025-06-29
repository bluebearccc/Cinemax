package com.bluebear.cinemax.service.promotion;

import com.bluebear.cinemax.dto.PromotionDTO;
import com.bluebear.cinemax.entity.Promotion;
import com.bluebear.cinemax.repository.PromotionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PromotionServiceImpl implements PromotionService{
    @Autowired
    private PromotionRepository promotionRepository;

    public PromotionDTO createPromotion(PromotionDTO dto) {
        Promotion promotion = toEntity(dto);
        return toDTO(promotionRepository.save(promotion));
    }

    public PromotionDTO getPromotionById(Integer id) {
        return promotionRepository.findById(id)
                .map(this::toDTO)
                .orElse(null);
    }

    public List<PromotionDTO> getAllPromotions() {
        return promotionRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public PromotionDTO updatePromotion(Integer id, PromotionDTO dto) {
        Optional<Promotion> optional = promotionRepository.findById(id);
        if (optional.isPresent()) {
            Promotion promotion = toEntity(dto);
            promotion.setPromotionID(id);
            return toDTO(promotionRepository.save(promotion));
        }
        return null;
    }

    public void deletePromotion(Integer id) {
        promotionRepository.deleteById(id);
    }

    public PromotionDTO toDTO(Promotion promotion) {
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

    public Promotion toEntity(PromotionDTO dto) {
        return Promotion.builder()
                .promotionID(dto.getPromotionID())
                .promotionCode(dto.getPromotionCode())
                .discount(dto.getDiscount())
                .startTime(dto.getStartTime())
                .endTime(dto.getEndTime())
                .quantity(dto.getQuantity())
                .status(dto.getStatus())
                .build();
    }
}
