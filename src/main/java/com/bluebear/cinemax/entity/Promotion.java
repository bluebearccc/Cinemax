package com.bluebear.cinemax.entity;

import com.bluebear.cinemax.enums.PromotionStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import java.time.LocalDateTime;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "Promotion")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Promotion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PromodtionID")
    private Integer promotionId;

    @Column(name = "PromotionCode", nullable = false, unique = true, length = 10)
    private String promotionCode;

    @Column(name = "Discount", nullable = false)
    private Integer discount;

    @Column(name = "StartTime", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "EndTime", nullable = false)
    private LocalDateTime endTime;

    @Column(name = "Quantity", nullable = false)
    private Integer quantity;

    @Enumerated(EnumType.STRING)
    @Column(name = "Status", length = 20)
    private PromotionStatus status;
}
