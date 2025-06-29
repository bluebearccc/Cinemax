package com.bluebear.cinemax.entity;

import com.bluebear.cinemax.enumtype.Promotion_Status;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "Promotion")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Promotion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PromotionID")
    private Integer promotionID;

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

    @Column(name = "Status", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private Promotion_Status status;

    @OneToMany(mappedBy = "promotion", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Invoice> invoices;
}
