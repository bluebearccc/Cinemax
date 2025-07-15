package com.bluebear.cinemax.service.promotion;

import com.bluebear.cinemax.dto.PromotionDTO;
import com.bluebear.cinemax.entity.Promotion;
import com.bluebear.cinemax.repository.*;
import com.bluebear.cinemax.service.EmailService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
@Service
public class PromotionServiceImp implements PromotionService {
    @Autowired
    private PromotionRepository promotionRepo;

    public Optional<PromotionDTO> validatePromotionCode(String code) {
        return promotionRepo.findByPromotionCode(code)
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
        Optional<Promotion> promoOpt = promotionRepo.findByPromotionCode(code);
        Map<String, Object> response = new HashMap<>();

        if (promoOpt.isPresent()) {
            Promotion promo = promoOpt.get();

            // Kiểm tra thời hạn và số lượng mã
            if (promo.getEndTime().isAfter(LocalDateTime.now()) && promo.getQuantity() > 0) {
                double discount = totalAmount * promo.getDiscount() / 100.0;

                // Cập nhật số lượng mã giảm giá
                promo.setQuantity(promo.getQuantity() - 1);
                promotionRepo.save(promo);

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
    public PromotionDTO toDTO(Promotion promotion) {
        return new PromotionDTO(
                promotion.getPromotionID(),
                promotion.getPromotionCode(),
                promotion.getDiscount(),
                promotion.getStartTime(),
                promotion.getEndTime(),
                promotion.getQuantity(),
                promotion.getStatus()
        );
    }

}
