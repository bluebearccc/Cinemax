package com.bluebear.cinemax.dto.cashier;

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
    private String status; // Dùng String để dễ dàng hiển thị hoặc map từ Enum
}