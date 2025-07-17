package com.bluebear.cinemax.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ItemRevenue {
    private String itemName;
    private BigDecimal totalRevenue;
}