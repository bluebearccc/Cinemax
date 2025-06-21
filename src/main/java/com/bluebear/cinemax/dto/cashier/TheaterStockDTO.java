package com.bluebear.cinemax.dto.cashier;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TheaterStockDTO {
    private Integer theaterStockId;
    private TheaterDTO theater;
    private String foodName;
    private Integer quantity;
    private Double unitPrice;
    private String image;
    private String status;
}
