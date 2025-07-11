package com.bluebear.cinemax.dto;

import com.bluebear.cinemax.enumtype.RedeemStatus;
import lombok.*;

import java.time.LocalDateTime;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RedeemHistoryDTO {
    private Integer redeemId;
    private Integer customerId;
    private Integer rewardItemId;
    private LocalDateTime redeemDate;
    private int quantity;
    private RedeemStatus status;
}
