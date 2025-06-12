package com.bluebear.cinemax.dto;

import com.bluebear.cinemax.enums.TheaterStock_Status;
import com.bluebear.cinemax.enums.Theater_Status;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TheaterStockDTO {
    private int id;
    private String itemName;
    private String image;
    private int quantity;
    private double price;
    private TheaterStock_Status status;
}
