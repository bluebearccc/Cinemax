package com.bluebear.cinemax.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DetailFDDTO {
    private Integer id;
    private Integer invoiceId;
    private Integer theaterStockId;
    private Integer quantity;
    private BigDecimal totalPrice;
    private TheaterStockDTO theaterStock;
}