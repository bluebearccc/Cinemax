package com.bluebear.cinemax.entity;

import com.bluebear.cinemax.enumtype.InvoiceStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "Invoice")
public class Invoice {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer invoiceId;

    @ManyToOne
    @JoinColumn(name = "CustomerID", nullable = false)
    private Customer customer;

    @ManyToOne
    @JoinColumn(name = "EmployeeID")
    private Employee employee;

    @ManyToOne
    @JoinColumn(name = "PromotionID")
    private Promotion promotion;

    @Column(name = "Discount", nullable = true)
    private Float discount;

    @Column(name = "BookingDate", nullable = false)
    private LocalDateTime bookingDate;

    @Column(name = "Totalprice", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalPrice;

    @Column(name = "Status", nullable = false)
    @Enumerated(EnumType.STRING)
    private InvoiceStatus status;
}
