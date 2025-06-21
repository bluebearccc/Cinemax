package com.bluebear.cinemax.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.persistence.*;
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "TheaterID", nullable = false)
    private Theater theater;

    @Column(name = "FoodName", nullable = false, length = 20)
    private String foodName;

    @Column(name = "Quantity", nullable = false)
    private Integer quantity;

    @Column(name = "UnitPrice", nullable = false)
    private Double unitPrice;

    @Column(name = "Image", nullable = false)
    private String image;

    @Enumerated(EnumType.STRING)
    @Column(name = "Status")
    private StockStatus status;

    @OneToMany(mappedBy = "theaterStock", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<DetailFD> detailFDs;

    public enum StockStatus {
        Active, Inactive
    }
}