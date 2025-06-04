package com.bluebear.cinemax.entity;

import com.bluebear.cinemax.enums.EmployeePosition;
import com.bluebear.cinemax.enums.TheaterStatus;
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
    private Integer employeeId;

    @Enumerated(EnumType.STRING)
    @Column(name = "Position")
    private EmployeePosition position;

    @Enumerated(EnumType.STRING)
    @Column(name = "Status", length = 20)
    private TheaterStatus status;

    @OneToOne
    @JoinColumn(name = "AccountID", nullable = false)
    private Account account;

    @ManyToOne
    @JoinColumn(name = "TheaterID", nullable = false)
    private Theater theater;

    @ManyToOne
    @JoinColumn(name = "AdminID")
    private Employee admin;

    @Column(name = "FullName", nullable = false, length = 100)
    private String fullName;

    @OneToMany(mappedBy = "admin", cascade = CascadeType.ALL)
    private List<Employee> subordinates;

    @OneToMany(mappedBy = "employee", cascade = CascadeType.ALL)
    private List<Invoice> invoices;
}
