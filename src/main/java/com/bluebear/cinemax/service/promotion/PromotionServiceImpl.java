package com.bluebear.cinemax.service.promotion;

import com.bluebear.cinemax.dto.PromotionDTO;
import com.bluebear.cinemax.entity.Promotion;
import com.bluebear.cinemax.enumtype.Promotion_Status;
import com.bluebear.cinemax.repository.PromotionRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PromotionServiceImpl implements PromotionService{
    @Autowired
    private PromotionRepository promotionRepository;
    public Optional<PromotionDTO> validatePromotionCode(String code) {
        return promotionRepository.findByPromotionCode(code)
                .filter(Promotion::isValid)
                .map(this::toDTO);
    }
    public Map<String, Object> checkPromotionCode(String code, double totalAmount) {
        Optional<PromotionDTO> promoOpt = validatePromotionCode(code);
        Map<String, Object> response = new HashMap<>();

        if (promoOpt.isPresent()) {
            PromotionDTO promo = promoOpt.get();
            double discount = totalAmount * promo.getDiscount() / 100.0;
            response.put("valid", true);
            response.put("discount", discount);
            response.put("message", "Mã hợp lệ! Bạn được giảm: " + discount + " VNĐ.");
        } else {
            response.put("valid", false);
            response.put("message", "Mã giảm giá không tồn tại hoặc đã hết hạn.");
        }
        return response;
    }
    @Transactional
    public Map<String, Object> applyPromotionCode(String code, double totalAmount) {
        Optional<Promotion> promoOpt = promotionRepository.findByPromotionCode(code);
        Map<String, Object> response = new HashMap<>();

        if (promoOpt.isPresent()) {
            Promotion promo = promoOpt.get();

            // Kiểm tra thời hạn và số lượng mã
            if (promo.getEndTime().isAfter(LocalDateTime.now()) && promo.getQuantity() > 0) {
                double discount = totalAmount * promo.getDiscount() / 100.0;

                // Cập nhật số lượng mã giảm giá
                promo.setQuantity(promo.getQuantity() - 1);
                promotionRepository.save(promo);

                response.put("valid", true);
                response.put("discount", discount);
                response.put("message", "Mã áp dụng thành công! Bạn được giảm: " + discount + " VNĐ.");
            } else {
                response.put("valid", false);
                response.put("message", "Mã giảm giá đã hết hạn hoặc không còn khả dụng.");
            }
        } else {
            response.put("valid", false);
            response.put("message", "Mã giảm giá không tồn tại.");
        }

        return response;
    }

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

    @Override
    public List<PromotionDTO> getActivePromotions() {
        return promotionRepository.findActivePromotions(Promotion_Status.Available, LocalDateTime.now())
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
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
