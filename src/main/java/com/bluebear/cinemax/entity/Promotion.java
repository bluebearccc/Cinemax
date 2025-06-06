package com.bluebear.cinemax.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "Promotion")
public class Promotion {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PromodtionID")
    private Integer promotionId;
    
    @Column(name = "PromotionCode", length = 10, nullable = false, unique = true)
    private String promotionCode;
    
    @Column(name = "Discount", nullable = false)
    private Integer discount;
    
    @Column(name = "StartTime", nullable = false)
    private LocalDateTime startTime;
    
    @Column(name = "EndTime", nullable = false)
    private LocalDateTime endTime;
    
    @Column(name = "Quantity", nullable = false)
    private Integer quantity;
    
    @Column(name = "Active", length = 20, nullable = false)
    private String active;
}