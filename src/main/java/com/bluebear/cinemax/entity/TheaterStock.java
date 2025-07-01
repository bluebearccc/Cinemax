package com.bluebear.cinemax.entity;
import com.bluebear.cinemax.enumtype.Movie_Status;
import com.bluebear.cinemax.enumtype.Theater_Status;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
@Data
@NoArgsConstructor
@AllArgsConstructor
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

    @Column(name = "UnitPrice", nullable = false)
    private Double unitPrice;

    @Column(name = "Image", length = 255, nullable = false)
    private String image;

    @Column(name = "Status", length = 20)
    @Enumerated(EnumType.STRING)
    private Theater_Status status;

    // Getters and Setters


}
