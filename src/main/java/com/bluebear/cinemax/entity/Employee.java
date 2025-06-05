package com.bluebear.cinemax.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Table(name = "Employee")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "EmployeeID")
    private Integer employeeID;

    @Column(name = "FullName")
    private String fullName;

    @Column(name = "Position")
    private String position;
    
    @Column(name = "Status")
    private String status;

    @OneToOne
    @JoinColumn(name = "AccountID", nullable = false)
    private Account account;

    @ManyToOne
    @JoinColumn(name = "TheaterID")
    private Theater theater;

    @ManyToOne
    @JoinColumn(name = "AdminID")
    private Employee admin;

    @OneToMany(mappedBy = "admin")
    private List<Employee> managedEmployees;
}

