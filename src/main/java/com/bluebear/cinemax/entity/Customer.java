package com.bluebear.cinemax.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Table(name = "Customer")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Customer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "CustomerID")
    private Integer customerId;

    @OneToOne
    @JoinColumn(name = "AccountID", nullable = false)
    private Account account;

    @Column(name = "FullName", nullable = false, length = 100)
    private String fullName;

    @Column(name = "Phone", length = 20)
    private String phone;

    @Column(name = "Point")
    private Integer point;

    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL)
    private List<Invoice> invoices;

    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL)
    private List<MovieFeedback> movieFeedbacks;

    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL)
    private List<ServiceFeedback> serviceFeedbacks;

}