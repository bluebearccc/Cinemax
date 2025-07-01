package com.bluebear.cinemax.dto;

import com.bluebear.cinemax.entity.TheaterStock;
import com.bluebear.cinemax.enumtype.Theater_Status;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TheaterStockDTO {
    private Integer theaterStockID;
    private int theater;
    private String foodName;
    private Integer quantity;
    private Double unitPrice;
    private String image;
    private Theater_Status status;

    // Constructors
//    public TheaterStockDTO(TheaterStock stock) {
//        this.theaterStockID = stock.getTheaterStockID();
//        this.theater = stock.getTheater();
//        this.foodName = stock.getFoodName();
//        this.quantity = stock.getQuantity();
//        this.unitPrice = stock.getUnitPrice()*1000;
//        this.image = stock.getImage();
//        this.status = stock.getStatus();
//    }



}
