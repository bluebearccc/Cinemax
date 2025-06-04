package com.bluebear.cinemax.dto;

import com.bluebear.cinemax.enums.TheaterStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TheaterStockDTO {
    private Integer theaterStockId;
    private Integer theaterId;
    private String foodName;
    private Integer quantity;
    private BigDecimal unitPrice;
    private String image;
    private TheaterStatus status;
    private TheaterDTO theater;
}
