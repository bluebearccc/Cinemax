package com.bluebear.cinemax.dto;

import com.bluebear.cinemax.enums.PromotionStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PromotionDTO {
    private Integer promotionId;
    private String promotionCode;
    private Integer discount;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer quantity;
    private PromotionStatus status;
}