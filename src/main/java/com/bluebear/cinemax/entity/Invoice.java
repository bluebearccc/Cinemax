package com.bluebear.cinemax.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.Set;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "Invoice")
public class Invoice {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "InvoiceID")
    private long invoiceID;

    @Column(name = "CustomerID")
    private String customerID;

    @Column(name = "EmployeeID")
    private String EmployeeID;

    @Column(name = "PromotionID")
    private String promotionID;

    @Column(name = "BookingDate")
    private Date bookingDate;

    @Column(name = "TotalPrice")
    private float totalPrice;

    @OneToMany(mappedBy = "invoice")
    private Set<Detail_FD> detail_FD;
}
