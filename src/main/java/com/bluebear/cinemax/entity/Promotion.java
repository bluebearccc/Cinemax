package com.bluebear.cinemax.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "Promotion")
@Getter
@Setter
public class Promotion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer promotionID;

    private String promotionCode;
    private Integer discount;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer quantity;
    private String status;

    public boolean isValid() {
        return quantity > 0 && endTime.isAfter(LocalDateTime.now());
    }
}
