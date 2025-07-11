package com.bluebear.cinemax.entity;

import com.bluebear.cinemax.enumtype.RedeemStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RedeemHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "RedeemID")
    private Integer redeemId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CustomerID", nullable = false)
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "RewardItemID", nullable = false)
    private RewardItem rewardItem;


    @Column(name = "RedeemDate", updatable = false)
    private LocalDateTime redeemDate;

    @Column(name = "Quantity", nullable = false)
    private int quantity;

    @Enumerated(EnumType.STRING)
    @Column(name = "Status", nullable = false, length = 20)
    private RedeemStatus status;
}
