package com.bluebear.cinemax.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.*;

import java.util.Objects;
@Data
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@Entity
@Table(name = "Customer")
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "CustomerID")
    private int customerId;


    @ManyToOne
    @JoinColumn(name = "AccountID", insertable = false, updatable = false)
    private Account account;

    @Column(name = "FullName", length = 100, nullable = false)
    private String fullName;

    @Column(name = "Phone", length = 20)
    private String phone;

    @Column(name= "Point")
    private int point;
    // Constructors


}
