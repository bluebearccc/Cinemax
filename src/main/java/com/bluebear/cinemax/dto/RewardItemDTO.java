package com.bluebear.cinemax.dto;

import com.bluebear.cinemax.enumtype.RewardItemStatus;
import com.bluebear.cinemax.enumtype.RewardItemType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RewardItemDTO {
    private Integer rewardItemId;
    private RewardItemType itemType;
    private Integer itemId;
    private int requiredPoints;
    private int quantityAvailable;
    private RewardItemStatus status;
}
