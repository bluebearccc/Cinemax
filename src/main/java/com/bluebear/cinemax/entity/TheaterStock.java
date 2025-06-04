package com.bluebear.cinemax.entity;

import com.bluebear.cinemax.enums.TheaterStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Entity
@Table(name = "Theater_Stock")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TheaterStock {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Theater_StockID")
    private Integer theaterStockId;

    @ManyToOne
    @JoinColumn(name = "TheaterID", nullable = false)
    private Theater theater;

    @Column(name = "FoodName", nullable = false, length = 20)
    private String foodName;

    @Column(name = "Quantity", nullable = false)
    private Integer quantity;

    @Column(name = "UnitPrice", nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPrice;

    @Column(name = "Image", nullable = false)
    private String image;

    @Enumerated(EnumType.STRING)
    @Column(name = "Status", length = 20)
    private TheaterStatus status;

    @OneToMany(mappedBy = "theaterStock", cascade = CascadeType.ALL)
    private List<DetailFD> detailFDs;
}
