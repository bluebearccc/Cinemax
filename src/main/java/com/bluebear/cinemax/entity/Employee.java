package com.bluebear.cinemax.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "Employee")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer employeeID;

    @Column(nullable = false, length = 255)
    private String position;

    @Column(nullable = false, length = 20)
    private String status;


    @Column(name = "accountID", nullable = false)
    private int account;


    @Column(name = "theaterID", nullable = false)
    private int theater;


    @Column(name = "adminID")
    private Boolean admin;

    @Column(nullable = false, length = 100)
    private String fullName;

}