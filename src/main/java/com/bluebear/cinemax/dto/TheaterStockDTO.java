package com.bluebear.cinemax.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TheaterStockDTO {
    private Integer theaterStockId;
    private TheaterDTO theater;
    private String foodName;
    private Integer quantity;
    private Double unitPrice;
    private String image;
    private String status;
}