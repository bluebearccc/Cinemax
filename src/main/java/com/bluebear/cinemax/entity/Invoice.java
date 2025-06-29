package com.bluebear.cinemax.entity;

import com.bluebear.cinemax.enumtype.Invoice_Status;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "Invoice")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Invoice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "InvoiceID")
    private Integer invoiceID;

    @ManyToOne
    @JoinColumn(name = "CustomerID")
    private Customer customer;

    @ManyToOne
    @JoinColumn(name = "EmployeeID")
    private Employee employee;

    @ManyToOne
    @JoinColumn(name = "PromotionID")
    private Promotion promotion;

    @Column(name = "Discount")
    private Float discount;

    @Column(name = "BookingDate", nullable = false)
    private LocalDateTime bookingDate;

    @Column(name = "Totalprice", nullable = false)
    private Double totalPrice;

    @Column(name = "GuestName", length = 20)
    private String guestName;

    @Column(name = "GuestEmail", length = 20)
    private String guestEmail;

    @Column(name = "GuestPhone", length = 20)
    private String guestPhone;

    @Enumerated(EnumType.STRING)
    @Column(name = "Status", nullable = false, length = 20)
    private Invoice_Status status;

    @OneToMany(mappedBy = "invoice", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DetailSeat> detailSeats;

}