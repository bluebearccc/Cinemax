package com.bluebear.cinemax.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Set;
@Builder
@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "Invoice")
public class Invoice {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "InvoiceID")
    private Integer invoiceID;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CustomerID" , nullable = false, referencedColumnName = "CustomerID")
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "EmployeeID" , nullable = false, referencedColumnName = "EmployeeID")
    private Employee employee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PromotionID" , nullable = false, referencedColumnName = "PromotionID")
    private Promotion promotion;

    @Column(name = "BookingDate")
    private LocalDateTime bookingDate;

    @Column(name = "TotalPrice")
    private Double totalPrice;

    @OneToMany(mappedBy = "invoice")
    private List<Detail_FD> detail_FD;


}
