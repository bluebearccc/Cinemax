package com.bluebear.cinemax.dto;

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
    private Integer discount;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer quantity;
    private Promotion_Status status;
}