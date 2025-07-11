package com.bluebear.cinemax.entity;
import com.bluebear.cinemax.enumtype.RewardItemStatus;
import com.bluebear.cinemax.enumtype.RewardItemType;
import jakarta.persistence.GeneratedValue;
import lombok.*;
import jakarta.persistence.*;
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "RewardItem")

public class RewardItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "RewardItemID")
    private Integer rewardItemId;

    @Enumerated(EnumType.STRING)
    @Column(name = "ItemType", nullable = false, length = 50)
    private RewardItemType itemType;

    @Column(name = "ItemID", nullable = false)
    private Integer itemId;

    @Column(name = "RequiredPoints", nullable = false)
    private int requiredPoints;

    @Column(name = "QuantityAvailable", nullable = false)
    private int quantityAvailable;

    @Enumerated(EnumType.STRING)
    @Column(name = "Status", nullable = false, length = 20)
    private RewardItemStatus status;
}
