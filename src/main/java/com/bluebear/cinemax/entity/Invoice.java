package com.bluebear.cinemax.entity;

import com.bluebear.cinemax.enumtype.InvoiceStatus;
import com.bluebear.cinemax.enumtype.PaymentMethod;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
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
    @JoinColumn(name = "CustomerID", referencedColumnName = "CustomerID")
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "EmployeeID", referencedColumnName = "EmployeeID")
    private Employee employee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PromotionID", referencedColumnName = "PromotionID")
    private Promotion promotion;

    @Column(name = "BookingDate")
    private LocalDateTime bookingDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "Status")
    private InvoiceStatus status;

    @Column(name = "TotalPrice")
    private Double totalPrice;

    @OneToMany(mappedBy = "invoice")
    private List<Detail_FD> detail_FD;

    @Column(name = "GuestName")
    private String guestName;

    @Column(name = "GuestPhone")
    private String guestPhone;

    @Column(name = "GuestEmail", nullable = true)
    private String guestEmail;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", length = 20)
    private PaymentMethod paymentMethod;

    @Column(columnDefinition = "TEXT")
    private String bookingDetails;
}
