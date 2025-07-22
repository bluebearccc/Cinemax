package com.bluebear.cinemax.entity;

import com.bluebear.cinemax.enumtype.DetailFD_Status;
import com.bluebear.cinemax.enumtype.InvoiceStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
@Builder
@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Detail_FD {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Integer id;

    @Column(name = "Quantity")
    private int quantity;

    @Column(name = "TotalPrice")
    private Double totalPrice;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "InvoiceID", referencedColumnName = "InvoiceID")
    private Invoice invoice;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "Theater_StockID", referencedColumnName = "Theater_StockID")
    private TheaterStock theaterStock;

    @Enumerated(EnumType.STRING)
    @Column(name = "Status")
    private DetailFD_Status status;
}
