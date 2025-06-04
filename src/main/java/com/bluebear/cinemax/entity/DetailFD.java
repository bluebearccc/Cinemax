package com.bluebear.cinemax.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "Detail_FD")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DetailFD {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "InvoiceID", nullable = false)
    private Invoice invoice;

    @ManyToOne
    @JoinColumn(name = "Theater_StockID", nullable = false)
    private TheaterStock theaterStock;

    @Column(name = "Quantity", nullable = false)
    private Integer quantity;

    @Column(name = "TotalPrice", precision = 10, scale = 2)
    private BigDecimal totalPrice;
}
