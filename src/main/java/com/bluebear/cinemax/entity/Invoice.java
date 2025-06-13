package com.bluebear.cinemax.entity;

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
    @JoinColumn(name = "customerId", nullable = false)
    private Customer customer;

    @ManyToOne
    @JoinColumn(name = "employeeId")
    private Employee employee;

    @ManyToOne
    @JoinColumn(name = "promotionId")
    private Promotion promotion;

    @Column
    private Double discount;

    @Column(nullable = false)
    private LocalDateTime bookingDate;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal totalprice;

    // Getters and setters
}
