package com.bluebear.cinemax.entity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Setter
@Getter
@Entity
@Data

@Table(name = "Detail_FD")
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
    @Column(name = "Status", nullable = false)
    private String status;
    // Getters and Setters

}

