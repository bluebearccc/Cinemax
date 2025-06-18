package com.bluebear.cinemax.dto;

import com.bluebear.cinemax.enumtype.Promotion_Status;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

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

    private List<InvoiceDTO> invoiceDTOList;
}