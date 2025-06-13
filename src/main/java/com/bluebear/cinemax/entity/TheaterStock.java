package com.bluebear.cinemax.entity;
import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "Theater_Stock")
public class TheaterStock {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Theater_StockID")
    private Integer theaterStockID;


    @Column(name = "TheaterID", nullable = false)
    private int theater;

    @Column(name = "FoodName", length = 20, nullable = false)
    private String foodName;

    @Column(name = "Quantity", nullable = false)
    private Integer quantity;

    @Column(name = "UnitPrice", precision = 10, scale = 2, nullable = false)
    private BigDecimal unitPrice;

    @Column(name = "Image", length = 255, nullable = false)
    private String image;

    @Column(name = "Status", length = 20)
    private String status;

    // Getters and Setters

    public Integer getTheaterStockID() {
        return theaterStockID;
    }

    public void setTheaterStockID(Integer theaterStockID) {
        this.theaterStockID = theaterStockID;
    }

    public int getTheater() {
        return theater;
    }

    public void setTheater(int theater) {
        this.theater = theater;
    }

    public String getFoodName() {
        return foodName;
    }

    public void setFoodName(String foodName) {
        this.foodName = foodName;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

}
