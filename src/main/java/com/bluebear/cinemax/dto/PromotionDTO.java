package com.bluebear.cinemax.dto;

import com.bluebear.cinemax.entity.Promotion;
import com.bluebear.cinemax.enumtype.Promotion_Status;
import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PromotionDTO {
    private Integer promotionID;
    private String promotionCode;
    private Double discount;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer quantity;
    private Promotion_Status status;

    public boolean isValid() {
        return quantity > 0 && endTime.isAfter(LocalDateTime.now());
    }
}