package com.bluebear.cinemax.dto;

import com.bluebear.cinemax.entity.Theater;
import com.bluebear.cinemax.entity.TheaterStock;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TheaterStockDTO {
    private Integer theaterStockId;
    private String foodName;
    private Integer quantity;
    private Double unitPrice;
    private String image;
    private String status;
    private TheaterDTO theater;

}