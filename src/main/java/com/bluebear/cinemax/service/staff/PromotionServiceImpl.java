package com.bluebear.cinemax.service.staff;

import com.bluebear.cinemax.dto.PromotionDTO;
import com.bluebear.cinemax.entity.Promotion;
import com.bluebear.cinemax.enumtype.Promotion_Status;
import com.bluebear.cinemax.repository.PromotionRepository; // Giả định bạn có PromotionRepository

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PromotionServiceImpl implements PromotionService {

    @Autowired
    private PromotionRepository promotionRepository; // Inject PromotionRepository

    /**
     * Chuyển đổi Promotion entity thành PromotionDTO.
     * @param promotion Entity Promotion.
     * @return PromotionDTO tương ứng.
     */
    private PromotionDTO convertToDTO(Promotion promotion) {
        if (promotion == null) {
            return null;
        }
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

    /**
     * Chuyển đổi PromotionDTO thành Promotion entity.
     * @param promotionDTO DTO Promotion.
     * @return Promotion entity tương ứng.
     */
    private Promotion convertToEntity(PromotionDTO promotionDTO) {
        if (promotionDTO == null) {
            return null;
        }

        Promotion promotion = new Promotion();
        promotion.setPromotionID(promotionDTO.getPromotionID());
        promotion.setPromotionCode(promotionDTO.getPromotionCode());
        promotion.setDiscount(promotionDTO.getDiscount());
        promotion.setStartTime(promotionDTO.getStartTime());
        promotion.setEndTime(promotionDTO.getEndTime());
        promotion.setQuantity(promotionDTO.getQuantity());
        promotion.setStatus(promotionDTO.getStatus());
        return promotion;
    }

    @Override
    public PromotionDTO getPromotionById(Integer id) {
        Optional<Promotion> promotionOptional = promotionRepository.findById(id);
        return promotionOptional.map(this::convertToDTO).orElse(null);
    }


}