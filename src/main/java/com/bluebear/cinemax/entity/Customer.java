package com.bluebear.cinemax.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "CustomerID")
    private Integer id;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "AccountID")
    private Account account;

    @Column(name = "FullName")
    private String fullName;

    @Column(name = "Phone")
    private String phone;

    public Customer(Account account, String fullName, String phone) {
        this.account = account;
        this.fullName = fullName;
        this.phone = phone;
    }
}
